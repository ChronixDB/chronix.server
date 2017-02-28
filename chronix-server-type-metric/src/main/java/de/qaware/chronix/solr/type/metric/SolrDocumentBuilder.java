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
package de.qaware.chronix.solr.type.metric;

import de.qaware.chronix.Schema;
import de.qaware.chronix.converter.common.Compression;
import de.qaware.chronix.converter.common.DoubleList;
import de.qaware.chronix.converter.common.LongList;
import de.qaware.chronix.converter.common.MetricTSSchema;
import de.qaware.chronix.converter.serializer.protobuf.ProtoBufMetricTimeSeriesSerializer;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.io.IOUtils;
import org.apache.solr.common.SolrDocument;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;

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
            ProtoBufMetricTimeSeriesSerializer.from(decompressed, tsStart, tsEnd, queryStart, queryEnd, ts);
            IOUtils.closeQuietly(decompressed);
        }
        return ts.build();
    }




}
