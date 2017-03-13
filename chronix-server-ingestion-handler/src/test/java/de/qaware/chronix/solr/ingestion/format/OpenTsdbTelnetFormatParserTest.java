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
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class OpenTsdbTelnetFormatParserTest {
    private OpenTsdbTelnetFormatParser sut;

    @Before
    public void setUp() throws Exception {
        sut = new OpenTsdbTelnetFormatParser();
    }

    @Test
    public void testParse() throws Exception {
        try (InputStream stream = GraphiteFormatParserTest.class.getResourceAsStream("/openTSDB-telnet.txt")) {
            assertNotNull(stream);
            List<MetricTimeSeries> series = Lists.newArrayList(sut.parse(stream));

            // We should have three metrics (CPU0, CPU1 and CPU2)
            assertThat(series.size(), is(3));

            // CPU 0 series has two points
            MetricTimeSeries cpu0series = findWithCpu(series, 0);
            assertThat(cpu0series.getName(), is("sys.cpu.user"));
            assertThat(cpu0series.getTimestamps().size(), is(2));
            assertThat(cpu0series.getTimestamps().get(0), is(1356998400000L));
            assertThat(cpu0series.getTimestamps().get(1), is(1356998401000L));
            assertThat(cpu0series.getAttributesReference().get("cpu"), is("0"));
            assertThat(cpu0series.getAttributesReference().get("host"), is("webserver01"));

            // CPU 1 series has only one point
            MetricTimeSeries cpu1series = findWithCpu(series, 1);
            assertThat(cpu1series.getName(), is("sys.cpu.user"));
            assertThat(cpu1series.getTimestamps().size(), is(1));
            assertThat(cpu1series.getTimestamps().get(0), is(1356998400000L));
            assertThat(cpu1series.getAttributesReference().get("cpu"), is("1"));
            assertThat(cpu1series.getAttributesReference().get("host"), is("webserver01"));

            // CPU 2 series has only one point
            MetricTimeSeries cpu2series = findWithCpu(series, 2);
            assertThat(cpu2series.getName(), is("sys.cpu.user"));
            assertThat(cpu2series.getTimestamps().size(), is(1));
            assertThat(cpu2series.getTimestamps().get(0), is(1356998400000L));
            assertThat(cpu2series.getAttributesReference().get("cpu"), is("2"));
            assertThat(cpu2series.getAttributesReference().get("host"), is("webserver01"));
        }
    }

    private MetricTimeSeries findWithCpu(Iterable<MetricTimeSeries> series, int num) {
        for (MetricTimeSeries s : series) {
            if (s.getAttributesReference().get("cpu").equals(Integer.toString(num))) {
                return s;
            }
        }

        throw new IllegalStateException("Series with cpu " + num + " not found");
    }
}