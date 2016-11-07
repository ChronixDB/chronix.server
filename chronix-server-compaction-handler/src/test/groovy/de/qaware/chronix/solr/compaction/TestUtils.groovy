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
package de.qaware.chronix.solr.compaction

import de.qaware.chronix.converter.common.Compression
import de.qaware.chronix.timeseries.MetricTimeSeries
import de.qaware.chronix.timeseries.dts.Point
import org.apache.lucene.document.*
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TopDocs
import org.apache.solr.common.SolrInputDocument
import org.hamcrest.Description
import org.hamcrest.TypeSafeDiagnosingMatcher

import static de.qaware.chronix.Schema.*
import static de.qaware.chronix.converter.common.MetricTSSchema.METRIC
import static de.qaware.chronix.converter.serializer.protobuf.ProtoBufMetricTimeSeriesSerializer.to

/**
 * Facilitates tsting.
 *
 * @author alex.christ
 */
final class TestUtils {

    /**
     * @param the points data points
     * @return compressed blob containing given points
     */
    public static final byte[] compress(Map<Long, Double> points) {
        def index = 0
        Compression.compress to(points.collect { new Point(index++, it.key, it.value) }.iterator())
    }

    /**
     * Creates a document from the given information.
     * Values for the fields
     * {@link de.qaware.chronix.Schema#START} and  {@link de.qaware.chronix.Schema#END} are calculated automatically.
     * The data is stored in a {@link #compress(java.util.Map)}ed format.
     *
     * @param metric the metric name
     * @param data the set of points
     * @return document
     */
    public static final Document doc(String metric, Map<Long, Double> data) {
        doc(metric, data.keySet().min(), data.keySet().max(), compress(data))
    }

    /**
     * @param metric the metric name
     * @param start the start
     * @param end the end
     * @param data the data
     * @return document
     */
    public static final Document doc(String metric, long start, long end, byte[] data) {
        new Document().with {
            add(new LongPoint(START, start))
            add(new LongPoint(END, end))
            add(new StoredField(DATA, data))
            add(new StringField(METRIC, metric, Field.Store.YES))
            (Document) it
        }
    }

    /**
     * Creates a matcher validating if a document contains all of the given attributes.
     *
     * @param attributes the attributes
     * @return hamcrest matcher
     */
    public static def hasAttributes(Map<String, Object> attributes) {
        new TypeSafeDiagnosingMatcher<SolrInputDocument>() {
            @Override
            protected boolean matchesSafely(SolrInputDocument item, Description mismatchDescription) {
                return attributes.each { assert item[it.key].value == it.value }
            }

            @Override
            void describeTo(Description description) {
                description.appendText('A document containing all of the given attributes')
            }
        }
    }

    /**
     * Creates a matcher validating if a time series contains all of the given attributes.
     *
     * @param attributes the attributes
     * @return hamcrest matcher
     */
    public static def timeseriesHasAttributes(Map<String, Object> attributes) {
        new TypeSafeDiagnosingMatcher<MetricTimeSeries>() {
            @Override
            protected boolean matchesSafely(MetricTimeSeries item, Description mismatchDescription) {
                return attributes.each { assert item[it.key] == it.value }
            }

            @Override
            void describeTo(Description description) {
                description.appendText('A time series containing all of the given attributes')
            }
        }
    }

    /**
     * @param docs the score docs
     * @return top docs containing given score docs
     */
    public static TopDocs asTopDocs(def docs) {
        new TopDocs(3, docs as ScoreDoc[], 0)
    }
}