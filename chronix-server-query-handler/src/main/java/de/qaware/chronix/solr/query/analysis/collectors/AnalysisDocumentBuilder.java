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
package de.qaware.chronix.solr.query.analysis.collectors;

import de.qaware.chronix.Schema;
import de.qaware.chronix.converter.BinaryStorageDocument;
import de.qaware.chronix.converter.KassiopeiaSimpleConverter;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author f.lautenschlager
 */
public class AnalysisDocumentBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisDocumentBuilder.class);

    private AnalysisDocumentBuilder() {
        //avoid instances
    }

    /**
     * Collects the given document and groups them using the join function result
     *
     * @param collectedDocs - the collected documents grouped by the join function result
     * @param currentDoc    - the document that should be collected
     * @param joinFunction  - the join function
     */
    public static void collect(Map<String, List<Document>> collectedDocs, Document currentDoc, Function<Document, String> joinFunction) {
        String key = joinFunction.apply(currentDoc);

        if (!collectedDocs.containsKey(key)) {
            collectedDocs.put(key, new ArrayList<>());
        }

        collectedDocs.get(key).add(currentDoc);
    }

    /**
     * @param aggregation - the isAggregation including its parameter
     * @param queryStart  - the user query start
     * @param queryEnd    - the user query end
     * @param docs        - the lucene documents that belong to the requested time series
     * @return the aggregated solr document
     */
    public static SolrDocument analyze(Map.Entry<AnalysisType, String[]> aggregation, long queryStart, long queryEnd, Map.Entry<String, List<Document>> docs) {
        MetricTimeSeries timeSeries = collectDocumentToTimeSeries(queryStart, queryEnd, docs);
        double value = analyzeTimeSeries(timeSeries, aggregation);
        return buildDocument(timeSeries, value, aggregation, docs.getKey());
    }


    /**
     * Collects the lucene documents into a single time series
     *
     * @param queryStart - the user query start
     * @param queryEnd   - the user query end
     * @param documents  - the lucene documents
     * @return a metric time series that holds all the points
     */
    private static MetricTimeSeries collectDocumentToTimeSeries(long queryStart, long queryEnd, Map.Entry<String, List<Document>> documents) {
        //Collect all document of a time series
        return documents.getValue().stream().map(tsDoc -> convert(tsDoc, queryStart, queryEnd))
                .collect(Collectors.reducing((t1, t2) -> {
                    t1.addAll(t2.getPoints());
                    return t1;
                })).get();
    }

    /**
     * Aggregates the metric time series using the given isAggregation
     *
     * @param timeSeries  - the time series
     * @param aggregation - the isAggregation
     * @return the aggregated value
     */
    private static double analyzeTimeSeries(MetricTimeSeries timeSeries, Map.Entry<AnalysisType, String[]> aggregation) {
        return AnalysisEvaluator.evaluate(timeSeries.getPoints(), aggregation.getKey(), aggregation.getValue());
    }

    /**
     * Builds a solr document that is needed for the response from the aggregated time series
     *
     * @param timeSeries  - the time series
     * @param value       - the isAggregation value
     * @param aggregation - the isAggregation
     * @param key         - the join key
     * @return a solr document holding the attributes and the aggregated value
     */
    private static SolrDocument buildDocument(MetricTimeSeries timeSeries, double value, Map.Entry<AnalysisType, String[]> aggregation, String key) {

        boolean highLevelAnalysis = AnalysisType.isHighLevel(aggregation.getKey());

        //-1 on high level analyses marks that the time series is ok and should not returned
        if (highLevelAnalysis && value < 0) {
            return null;
        }

        SolrDocument doc;

        if (highLevelAnalysis) {
            doc = convert(timeSeries, true);
        } else {
            doc = convert(timeSeries, false);
            doc.put("value", value);
        }

        //Add some information about the analysis
        doc.put("analysis", aggregation.getKey().name());
        doc.put("analysisParam", String.join("-", aggregation.getValue()));

        //add the join key
        doc.put("joinKey", key);

        return doc;
    }


    /**
     * Converts the given Lucene document in a metric time series
     *
     * @param doc        - the lucene document
     * @param queryStart - the query start
     * @param queryEnd   - the query end
     * @return a metric time series
     */
    private static MetricTimeSeries convert(Document doc, long queryStart, long queryEnd) {
        BinaryStorageDocument.Builder solrDocument = new BinaryStorageDocument.Builder();
        doc.forEach(field -> solrDocument.field(field.name(), evaluateRawType(field)));

        KassiopeiaSimpleConverter converter = new KassiopeiaSimpleConverter();
        return converter.from(solrDocument.build(), queryStart, queryEnd);
    }

    private static SolrDocument convert(MetricTimeSeries timeSeries, boolean withData) {
        KassiopeiaSimpleConverter converter = new KassiopeiaSimpleConverter();

        SolrDocument doc = new SolrDocument();
        converter.to(timeSeries).getFields().forEach((name, value) -> {
            if (name.equals(Schema.DATA) && withData) {
                doc.addField(name, value);
            } else {
                doc.addField(name, value);
            }
        });
        return doc;
    }

    /**
     * Solr returns a stored field instead of the data value.
     * It do not provide a method that indicate the value type (e.g., long or string)
     * thus we have do it manually :-(
     *
     * @param value - stored field.
     * @return an object as primitive java value
     */
    private static Object evaluateRawType(IndexableField value) {

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
