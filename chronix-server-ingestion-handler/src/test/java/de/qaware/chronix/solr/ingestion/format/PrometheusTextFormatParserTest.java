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
package de.qaware.chronix.solr.ingestion.format;

import com.google.common.collect.Lists;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class PrometheusTextFormatParserTest {
    private PrometheusTextFormatParser sut;
    private static final Instant NOW = Instant.now();

    @Before
    public void setUp() throws Exception {
        Clock clock = () -> NOW;
        sut = new PrometheusTextFormatParser(clock);
    }

    @Test
    public void testParse() throws Exception {
        try (InputStream stream = GraphiteFormatParserTest.class.getResourceAsStream("/prometheus-text.txt")) {
            assertNotNull(stream);
            List<MetricTimeSeries> series = Lists.newArrayList(sut.parse(stream));

            assertThat(series.size(), is(3));

            MetricTimeSeries metricWithoutTimestampAndLabel = series.stream().filter(s -> s.getName().equals("metric_without_timestamp_and_labels")).findFirst().get();
            assertThat(metricWithoutTimestampAndLabel.getTime(0), is(NOW.toEpochMilli()));
            assertThat(metricWithoutTimestampAndLabel.getValue(0), is(12.47));

            MetricTimeSeries httpRequestsTotal200 = series.stream().filter(s -> s.getName().equals("http_requests_total") && s.getAttributesReference().get("code").equals("200")).findFirst().get();
            assertThat(httpRequestsTotal200.getTime(0), is(1395066363000L));
            assertThat(httpRequestsTotal200.getValue(0), is(1027.0));

            MetricTimeSeries httpRequestsTotal400 = series.stream().filter(s -> s.getName().equals("http_requests_total") && s.getAttributesReference().get("code").equals("400")).findFirst().get();
            assertThat(httpRequestsTotal400.getTime(0), is(1395066363000L));
            assertThat(httpRequestsTotal400.getValue(0), is(3.0));
        }
    }
}