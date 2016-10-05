/*
 * Copyright (C) 2016 QAware GmbH
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
package de.qaware.chronix.solr.query.analysis;

import com.google.common.base.Strings;
import de.qaware.chronix.Schema;
import de.qaware.chronix.converter.common.Compression;
import de.qaware.chronix.converter.common.DoubleList;
import de.qaware.chronix.converter.common.LongList;
import de.qaware.chronix.converter.common.MetricTSSchema;
import de.qaware.chronix.converter.serializer.JsonKassiopeiaSimpleSerializer;
import de.qaware.chronix.converter.serializer.ProtoBufKassiopeiaSimpleSerializer;
import de.qaware.chronix.solr.query.ChronixQueryParams;
import de.qaware.chronix.solr.query.analysis.functions.ChronixAggregation;
import de.qaware.chronix.solr.query.analysis.functions.ChronixAnalysis;
import de.qaware.chronix.solr.query.analysis.functions.ChronixTransformation;
import de.qaware.chronix.solr.query.analysis.functions.FunctionValueMap;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.io.IOUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Function;

/**
 * Class to build documents using the given analysis or aggregation
 *
 * @author f.lautenschlager
 */
public final class SolrDocumentBuilder {

    private SolrDocumentBuilder() {
        //avoid instances
    }

    /**
     * Collects the given document and groups them using the join function result
     *
     * @param docs         the found documents that should be grouped by the join function
     * @param joinFunction the join function
     * @return the grouped documents
     */
    public static Map<String, List<SolrDocument>> collect(SolrDocumentList docs, Function<SolrDocument, String> joinFunction) {
        Map<String, List<SolrDocument>> collectedDocs = new HashMap<>();

        for (SolrDocument doc : docs) {
            String key = joinFunction.apply(doc);

            if (!collectedDocs.containsKey(key)) {
                collectedDocs.put(key, new ArrayList<>());
            }

            collectedDocs.get(key).add(doc);
        }


        return collectedDocs;
    }

    /**
     * Builds a solr document that is needed for the response.
     * If the functions contains only analyses an every analysis result is false the method returns null.
     * <p>
     * Transformations -> Return the time series
     * Aggregations -> Return the document
     * Analyses -> Return the document if a analysis result is positive
     *
     * @param timeSeries         the time series
     * @param functionValues     a map with executed analyses and values
     * @param key                the join key
     * @param dataShouldReturned true if the data should be returned, otherwise false
     * @param dataAsJson         if true, the data is returned as json
     * @return the time series as solr document
     */
    public static SolrDocument buildDocument(MetricTimeSeries timeSeries, FunctionValueMap functionValues, String key, boolean dataShouldReturned, boolean dataAsJson) {

        //Convert the document
        SolrDocument doc = convert(timeSeries, dataShouldReturned, dataAsJson);
        //add the join key
        doc.put(ChronixQueryParams.JOIN_KEY, key);
        //Only add if we have function values
        if (functionValues != null) {
            //Add the function results
            addAnalysesAndResults(functionValues, doc);
        }

        return doc;
    }

    /**
     * Add the functions and its results to the given solr document
     *
     * @param functionValueMap the function value map with the functions and the results
     * @param doc              the solr document to add the result
     */
    private static void addAnalysesAndResults(FunctionValueMap functionValueMap, SolrDocument doc) {

        //For identification purposes
        int counter = 0;

        //add the transformation information
        for (int transformation = 0; transformation < functionValueMap.sizeOfTransformations(); transformation++) {
            ChronixTransformation chronixTransformation = functionValueMap.getTransformation(transformation);
            doc.put(counter + "_" + ChronixQueryParams.FUNCTION + "_" + chronixTransformation.getType().name().toLowerCase(), chronixTransformation.getArguments());
            counter++;
        }

        //add the aggregation information
        for (int aggregation = 0; aggregation < functionValueMap.sizeOfAggregations(); aggregation++) {
            ChronixAggregation chronixAggregation = functionValueMap.getAggregation(aggregation);
            double value = functionValueMap.getAggregationValue(aggregation);
            doc.put(counter + "_" + ChronixQueryParams.FUNCTION + "_" + chronixAggregation.getType().name().toLowerCase(), value);

            //Only if arguments exists
            if (chronixAggregation.getArguments().length != 0) {
                doc.put(counter + "_" + ChronixQueryParams.FUNCTION_ARGUMENTS + "_" + chronixAggregation.getType().name().toLowerCase(), chronixAggregation.getArguments());
            }
            counter++;
        }

        //add the analyses information
        for (int analysis = 0; analysis < functionValueMap.sizeOfAnalyses(); analysis++) {
            ChronixAnalysis chronixAnalysis = functionValueMap.getAnalysis(analysis);
            boolean value = functionValueMap.getAnalysisValue(analysis);
            String identifier = functionValueMap.getAnalysisIdentifier(analysis);
            String nameWithLeadingUnderscore;

            //Check if there is an identifier
            if (Strings.isNullOrEmpty(identifier)) {
                nameWithLeadingUnderscore = "_" + chronixAnalysis.getType().name().toLowerCase();
            } else {
                nameWithLeadingUnderscore = "_" + chronixAnalysis.getType().name().toLowerCase() + "_" + identifier;
            }

            //Add some information about the analysis
            doc.put(counter + "_" + ChronixQueryParams.FUNCTION + nameWithLeadingUnderscore, value);

            //Only if arguments exists
            if (chronixAnalysis.getArguments().length != 0) {
                doc.put(counter + "_" + ChronixQueryParams.FUNCTION_ARGUMENTS + nameWithLeadingUnderscore, chronixAnalysis.getArguments());
            }
            counter++;
        }
    }

    /**
     * Collects the documents into a single time series.
     * Merges the time series attributes using a {@link Set}.
     * Arrays are added as a single entry in the result attributes.
     *
     * @param queryStart the user query start
     * @param queryEnd   the user query end
     * @param documents  the lucene documents
     * @param decompress marks if the data is requested and should be decompressed
     * @return a metric time series that holds all the points
     */
    public static MetricTimeSeries reduceDocumentToTimeSeries(long queryStart, long queryEnd, List<SolrDocument> documents, boolean decompress) {
        //Collect all document of a time series

        LongList timestamps = null;
        DoubleList values = null;
        Map<String, Object> attributes = new HashMap<>();
        String metric = null;

        for (SolrDocument doc : documents) {
            MetricTimeSeries ts = convert(doc, queryStart, queryEnd, decompress);

            //only if we decompress the data.
            if (decompress) {
                //Performance optimization. Avoiding fine grained growing.
                if (timestamps == null) {
                    int size = ts.size();
                    if (size < 1000) {
                        //well we have a small time series
                        size = 1000;
                    }
                    int calcAmountOfPoints = documents.size() * size;
                    timestamps = new LongList(calcAmountOfPoints);
                    values = new DoubleList(calcAmountOfPoints);
                }

                timestamps.addAll(ts.getTimestampsAsArray());
                values.addAll(ts.getValuesAsArray());
            }

            //we use the metric of the first time series.
            //metric is the default join key.
            if (metric == null) {
                metric = ts.getMetric();
            }
            merge(attributes, ts.getAttributesReference());
        }

        return new MetricTimeSeries.Builder(metric)
                .points(timestamps, values)
                .attributes(attributes)
                .build();
    }

    /**
     * Merges to sets of time series attributes.
     * The result is set for each key holding the values.
     * If the other value is a collection, than all values
     * of the collection are added instead of the collection object.
     *
     * @param merged     the merged attributes
     * @param attributes the attributes of the other time series
     */
    private static void merge(Map<String, Object> merged, Map<String, Object> attributes) {

        for (HashMap.Entry<String, Object> newEntry : attributes.entrySet()) {

            String key = newEntry.getKey();

            //we ignore the version in the result
            if (key.equals(ChronixQueryParams.SOLR_VERSION_FIELD)) {
                continue;
            }

            if (!merged.containsKey(key)) {
                merged.put(key, new LinkedHashSet());
            }

            LinkedHashSet values = (LinkedHashSet) merged.get(key);
            Object value = newEntry.getValue();

            //Check if the value is a collection.
            //If it is a collection we add all values instead of adding a collection object
            if (value instanceof Collection && !values.contains(value)) {
                values.addAll((Collection) value);
            } else if (!values.contains(value)) {
                //Otherwise we have a single value or an array.
                values.add(value);
            }
            //otherwise we ignore the value
        }
    }

    /**
     * Converts the given solr document in a metric time series
     *
     * @param doc        the lucene document
     * @param queryStart the query start
     * @param queryEnd   the query end
     * @param decompress marks if the data is requested and hence we have to decompress it or not
     * @return a metric time series
     */
    private static MetricTimeSeries convert(SolrDocument doc, long queryStart, long queryEnd, boolean decompress) {

        String metric = doc.getFieldValue(MetricTSSchema.METRIC).toString();
        long tsStart = (long) doc.getFieldValue(Schema.START);
        long tsEnd = (long) doc.getFieldValue(Schema.END);
        byte[] data = ((ByteBuffer) doc.getFieldValue(Schema.DATA)).array();

        MetricTimeSeries.Builder ts = new MetricTimeSeries.Builder(metric);

        for (Map.Entry<String, Object> field : doc) {
            if (MetricTSSchema.isUserDefined(field.getKey())) {
                if (field.getValue() instanceof ByteBuffer) {
                    ts.attribute(field.getKey(), ((ByteBuffer) field.getValue()).array());
                } else {
                    ts.attribute(field.getKey(), field.getValue());
                }

            }
        }
        //No data is requested, hence we do not decompress it
        if (decompress) {
            InputStream decompressed = Compression.decompressToStream(data);
            ProtoBufKassiopeiaSimpleSerializer.from(decompressed, tsStart, tsEnd, queryStart, queryEnd, ts);
            IOUtils.closeQuietly(decompressed);
        }
        return ts.build();
    }

    /**
     * Converts the given time series in a solr document
     *
     * @param timeSeries the time series
     * @param withData   flag to indicate if with data or without
     * @param asJson     flag to mark if the data should be returned as json
     * @return the filled solr document
     */
    private static SolrDocument convert(MetricTimeSeries timeSeries, boolean withData, boolean asJson) {

        SolrDocument doc = new SolrDocument();

        if (withData) {
            byte[] data;
            //ensure that the returned data is sorted
            timeSeries.sort();
            //data should returned serialized as json
            if (asJson) {
                data = new JsonKassiopeiaSimpleSerializer().toJson(timeSeries);
                doc.setField(ChronixQueryParams.DATA_AS_JSON, new String(data, Charset.forName("UTF-8")));
            } else {
                data = ProtoBufKassiopeiaSimpleSerializer.to(timeSeries.points().iterator());
                //compress data
                data = Compression.compress(data);
                doc.addField(Schema.DATA, data);
            }
        }

        timeSeries.attributes().forEach(doc::addField);
        //add the metric field as it is not stored in the attributes
        doc.addField(MetricTSSchema.METRIC, timeSeries.getMetric());
        doc.addField(Schema.START, timeSeries.getStart());
        doc.addField(Schema.END, timeSeries.getEnd());

        return doc;
    }


}
