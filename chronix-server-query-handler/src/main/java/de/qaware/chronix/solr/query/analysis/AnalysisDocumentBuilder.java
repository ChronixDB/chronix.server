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

import de.qaware.chronix.Schema;
import de.qaware.chronix.converter.KassiopeiaSimpleConverter;
import de.qaware.chronix.converter.common.MetricTSSchema;
import de.qaware.chronix.converter.serializer.ProtoBufKassiopeiaSimpleSerializer;
import de.qaware.chronix.solr.query.analysis.functions.AnalysisType;
import de.qaware.chronix.solr.query.analysis.functions.ChronixAnalysis;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import de.qaware.chronix.timeseries.dt.DoubleList;
import de.qaware.chronix.timeseries.dt.LongList;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Class to build documents using the given analysis or aggregation
 *
 * @author f.lautenschlager
 */
public final class AnalysisDocumentBuilder {

    private AnalysisDocumentBuilder() {
        //avoid instances
    }

    /**
     * Collects the given document and groups them using the join function result
     *
     * @param docs         - the found documents that should be grouped by the join function
     * @param joinFunction - the join function
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
     * Analyzes the given chunks of time series that match the given join key
     *
     * @param analysis   the analysis including the arguments
     * @param timeSeries the time series that is analyzed
     * @return the analyzed solr document
     */
    public static SolrDocument analyze(ChronixAnalysis analysis, String joinKey, MetricTimeSeries timeSeries) {
        double value = analysis.execute(timeSeries);
        return buildDocument(timeSeries, value, analysis, joinKey);
    }

    /**
     * Analyzes the given chunk sets of the two time series
     *
     * @param analysis      the analysis including the arguments
     * @param timeSeries    the time series from the first query
     * @param subTimeSeries one time series from the sub query (represented in the result)
     * @return the analyzed solr document
     */
    public static SolrDocument analyze(ChronixAnalysis analysis, String joinKey, MetricTimeSeries timeSeries, MetricTimeSeries subTimeSeries) {
        double value = analysis.execute(timeSeries, subTimeSeries);
        return buildDocument(subTimeSeries, value, analysis, joinKey);
    }

    /**
     * Collects the documents into a single time series
     *
     * @param queryStart - the user query start
     * @param queryEnd   - the user query end
     * @param documents  - the lucene documents
     * @return a metric time series that holds all the points
     */
    public static MetricTimeSeries collectDocumentToTimeSeries(long queryStart, long queryEnd, List<SolrDocument> documents) {
        //Collect all document of a time series

        LongList timestamps = new LongList();
        DoubleList values = new DoubleList();
        Map<String, Object> attributes = new HashMap<>();
        String metric = null;

        for (SolrDocument doc : documents) {
            MetricTimeSeries ts = convert(doc, queryStart, queryEnd);

            timestamps.addAll(ts.getTimestamps());
            values.addAll(ts.getValues());

            if (metric == null) {
                metric = ts.getMetric();
            }

            if (attributes.isEmpty()) {
                attributes.putAll(ts.attributes());
            }
        }

        return new MetricTimeSeries.Builder(metric)
                .points(timestamps, values)
                .attributes(attributes)
                .build();
    }

    /**
     * Builds a solr document that is needed for the response from the aggregated time series
     *
     * @param timeSeries      - the time series
     * @param value           - the isAggregation value
     * @param chronixAnalysis - the isAggregation
     * @param key             - the join key
     * @return a solr document holding the attributes and the aggregated value
     */
    private static SolrDocument buildDocument(MetricTimeSeries timeSeries, double value, ChronixAnalysis chronixAnalysis, String key) {

        boolean highLevelAnalysis = AnalysisType.isHighLevel(chronixAnalysis.getType());

        //-1 on high level analyses marks that the time series is ok and should not returned
        if (highLevelAnalysis && value < 0) {
            return null;
        }

        SolrDocument doc = convert(timeSeries, highLevelAnalysis);

        //Add some information about the analysis
        doc.put("analysis", chronixAnalysis.getType().name());
        doc.put("analysisParam", chronixAnalysis.getArguments());
        doc.put("value", value);

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
    private static MetricTimeSeries convert(SolrDocument doc, long queryStart, long queryEnd) {


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

        ProtoBufKassiopeiaSimpleSerializer.from(data, tsStart, tsEnd, queryStart, queryEnd, ts);
        return ts.build();
    }


    private static SolrDocument convert(MetricTimeSeries timeSeries, boolean withData) {

        SolrDocument doc = new SolrDocument();

        if (withData) {
            new KassiopeiaSimpleConverter().to(timeSeries).getFields().forEach(doc::addField);
        } else {
            timeSeries.attributes().forEach(doc::addField);
            //add the metric field as it is not stored in the attributes
            doc.addField(MetricTSSchema.METRIC, timeSeries.getMetric());
            doc.addField(Schema.START, timeSeries.getStart());
            doc.addField(Schema.END, timeSeries.getEnd());
        }

        return doc;
    }

}
