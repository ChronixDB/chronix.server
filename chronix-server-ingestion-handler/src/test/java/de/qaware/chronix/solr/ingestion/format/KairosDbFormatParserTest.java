/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
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

public class KairosDbFormatParserTest {
    private KairosDbFormatParser sut;

    @Before
    public void setUp() throws Exception {
        sut = new KairosDbFormatParser();
    }

    @Test
    public void test() throws Exception {
        try (InputStream stream = KairosDbFormatParserTest.class.getResourceAsStream("/kairosdb.json")) {
            assertNotNull(stream);
            List<MetricTimeSeries> series = Lists.newArrayList(sut.parse(stream));

            // We should have two metrics
            assertThat(series.size(), is(2));

            MetricTimeSeries first = series.get(0);
            assertThat(first.getMetric(), is("archive.file.tracked"));
            assertThat(first.getTimestamps().size(), is(1));
            assertThat(first.getValues().get(0), is(123.0));
            assertThat(first.getTimestamps().get(0), is(1349109376L));
            assertThat(first.getAttributesReference().get("host"), is("test"));

            MetricTimeSeries second = series.get(1);
            assertThat(second.getMetric(), is("archive.file.search"));
            assertThat(second.getTimestamps().size(), is(1));
            assertThat(second.getValues().get(0), is(32.1));
            assertThat(second.getTimestamps().get(0), is(1349109665L));
            assertThat(second.getAttributesReference().get("host"), is("test"));
        }
    }
}