/*
 * Copyright (C) 2015 QAware GmbH
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package de.qaware.chronix.analysis.aggregation;

import de.qaware.chronix.analysis.aggregation.math.Percentile;
import de.qaware.chronix.analysis.aggregation.math.StdDev;
import de.qaware.chronix.converter.BinaryStorageDocument;
import de.qaware.chronix.converter.KassiopeiaSimpleConverter;
import de.qaware.chronix.dts.MetricDataPoint;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReader;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.search.DelegatingCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * AggregationPlugin collector for chronix-kassiopeia-simple time series.
 * Allows
 * - max
 * - min
 * - avg
 * - dev
 * - percentile
 * aggregation.
 *
 * @author f.lautenschlager
 */
public class AggregationCollector extends DelegatingCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregationCollector.class);
    public static final String METRIC = "metric";

    private final ResponseBuilder responseBuilder;

    private final Map<String, List<Document>> collectedDocs;
    private final long queryStart;
    private final long queryEnd;

    //min, max, avg,
    private final String aggregation;
    //used for aggregation like p=90
    private final String unmodifiedAggregation;
    private final double aggregationValue;

    /**
     * Constructs an aggregation collector
     *
     * @param rb          - the response builder
     * @param aggregation - the aggregation (min,max,avg,dev,p)
     * @param queryStart  - the start of the query
     * @param queryEnd    - the end of the query
     */
    public AggregationCollector(final ResponseBuilder rb, final String aggregation, final long queryStart, final long queryEnd) {
        this.responseBuilder = rb;
        this.collectedDocs = new HashMap<>();

        if (queryStart == -1) {
            this.queryStart = 0;
        } else {
            this.queryStart = queryStart;
        }

        if (queryEnd == -1) {
            this.queryEnd = Long.MAX_VALUE;
        } else {
            this.queryEnd = queryEnd;
        }

        this.unmodifiedAggregation = aggregation;
        if (aggregation.contains("=")) {
            this.aggregationValue = Double.parseDouble(aggregation.substring(aggregation.indexOf("=") + 1));
            this.aggregation = aggregation.substring(0, aggregation.indexOf("="));
        } else {
            this.aggregation = aggregation;
            this.aggregationValue = -1;
        }

    }

    /**
     * Collects all documents that belong to a metric
     * and collects them in a list.
     *
     * @param docNumber - the number of the current document
     * @throws IOException - not thrown
     */
    public void collect(int docNumber) throws IOException {

        // To be able to get documents, we need the reader
        LeafReader reader = context.reader();

        // From the reader we get the current document by the docNumber
        Document currentDoc = reader.document(docNumber);

        //First get the metric as it is our hard coded key
        String key = currentDoc.get(METRIC);

        if (!collectedDocs.containsKey(key)) {
            collectedDocs.put(key, new ArrayList<>());
        }

        collectedDocs.get(key).add(currentDoc);

    }

    /**
     * The finish phase does the aggregation on the prior collected documents.
     * It converts the documents into time series and calculates the value
     *
     * @throws IOException - if a delegating collector throws one
     */
    public void finish() throws IOException {

        Map<String, Map> aggregatedResultDocs = new HashMap<>();

        //First decompress all documents
        collectedDocs.entrySet().parallelStream().forEach(entry -> {

                    //Collect all document of a time series
                    MetricTimeSeries timeSeries = entry.getValue().stream().map(this::convert)
                            .collect(Collectors.reducing((t1, t2) -> {
                                t1.addAll(t2.getPoints());
                                return t1;
                            })).get();

                    double value = -1;

                    if (timeSeries.size() > 0) {
                        DoubleStream result = timeSeries.getPoints().stream().mapToDouble(MetricDataPoint::getValue);
                        //now lets aggregate them
                        switch (aggregation) {
                            case "avg":
                                value = result.average().getAsDouble();
                                break;
                            case "min":
                                value = result.min().getAsDouble();
                                break;
                            case "max":
                                value = result.max().getAsDouble();
                                break;
                            case "dev":
                                value = StdDev.dev(result.boxed().collect(Collectors.toList()));
                                break;
                            case "p":
                                value = Percentile.evaluate(result, aggregationValue);
                                break;
                            default:
                                //nothing
                        }
                    }

                    Map<String, Object> resultFields = new HashMap<>();

                    timeSeries.attributes().forEach(resultFields::put);

                    //add the required fields
                    resultFields.put("metric", entry.getKey());
                    resultFields.put("start", timeSeries.getStart());
                    resultFields.put("end", timeSeries.getEnd());

                    //set the aggregation result
                    resultFields.put("value", value);
                    resultFields.put("type", unmodifiedAggregation);

                    aggregatedResultDocs.put(entry.getKey(), resultFields);
                }

        );


        NamedList analytics = new NamedList();
        responseBuilder.rsp.add("aggregation", analytics);
        aggregatedResultDocs.forEach(analytics::add);

        if (this.delegate instanceof DelegatingCollector) {
            ((DelegatingCollector) this.delegate).finish();
        }

    }

    private MetricTimeSeries convert(Document doc) {
        BinaryStorageDocument.Builder solrDocument = new BinaryStorageDocument.Builder();
        doc.forEach((field) -> solrDocument.field(field.name(), evaluateRawType(field)));

        KassiopeiaSimpleConverter converter = new KassiopeiaSimpleConverter();
        return converter.from(solrDocument.build(), queryStart, queryEnd);
    }

    /**
     * Solr returns a stored field instead of the data value.
     * It do not provide a method that indicate the value type (e.g., long or string)
     * thus we have do it manually :-(
     *
     * @param value - stored field.
     * @return an object as primitive java value
     */
    private Object evaluateRawType(IndexableField value) {

        if (value.binaryValue() != null) {
            return value.binaryValue().bytes;

        } else if (value.numericValue() != null) {
            return value.numericValue();

        } else if (value.stringValue() != null) {
            return value.stringValue();

        } else {
            LOGGER.warn("Could not determine type of field {}. Returning null as value", value);
            return null;
        }

    }
}
