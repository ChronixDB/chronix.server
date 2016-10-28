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
package de.qaware.chronix.solr

import de.qaware.chronix.converter.common.Compression
import de.qaware.chronix.timeseries.MetricTimeSeries
import de.qaware.chronix.timeseries.dts.Point
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.common.params.ModifiableSolrParams
import org.apache.solr.common.params.SolrParams

import static de.qaware.chronix.Schema.ID
import static de.qaware.chronix.converter.serializer.protobuf.ProtoBufMetricTimeSeriesSerializer.from
import static de.qaware.chronix.converter.serializer.protobuf.ProtoBufMetricTimeSeriesSerializer.to
import static java.util.UUID.randomUUID

/**
 * Test utilities.
 *
 * @author alex.christ
 */
class TestUtils {

    /**
     * @param keyValuePairs field value pairs
     * @return solr input document
     */
    public static SolrInputDocument doc(Map<String, Object> keyValuePairs) {
        SolrInputDocument result = new SolrInputDocument()
        result.addField(ID, randomUUID().toString())
        keyValuePairs.each { result.addField(it.key, it.value) }
        result
    }

    /**
     * @param keyValuePairs option value pairs
     * @return solr params
     */
    public static SolrParams params(Map<String, Object> keyValuePairs) {
        keyValuePairs.inject(new ModifiableSolrParams()) { ModifiableSolrParams res, pair -> res.set(pair.key, pair.value) }
    }

    /**
     * @param the points data points
     * @return compressed blob containing given points
     */
    public static final byte[] compress(Map<Long, Double> points) {
        def index = 0
        Compression.compress to(points.collect { new Point(index++, it.key, it.value) }.iterator())
    }

    /**
     * @param bytes result of {@link #compress(java.util.Map)}
     * @param start start of time series
     * @param end end of time series
     * @return ( timestamp , value ) pairs
     */
    public static final Map<Long, Double> decompress(byte[] bytes, int start, int end) {
        def builder = new MetricTimeSeries.Builder('')
        from new ByteArrayInputStream(Compression.decompress(bytes)), start, end, builder
        builder.build().points().collect { it -> [(it.getTimestamp()): it.getValue()] }.collectEntries()
    }
}