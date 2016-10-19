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

public class GraphiteFormatParserTest {
    private GraphiteFormatParser sut;

    @Before
    public void setUp() throws Exception {
        sut = new GraphiteFormatParser();
    }

    @Test
    public void testParse() throws Exception {
        try (InputStream stream = GraphiteFormatParserTest.class.getResourceAsStream("/graphite.txt")) {
            assertNotNull(stream);
            List<MetricTimeSeries> series = Lists.newArrayList(sut.parse(stream));

            // We should have two metrics
            assertThat(series.size(), is(2));

            // test.bash.stats has 5 timestamps with 5 values
            MetricTimeSeries bashSeries = series.get(0);
            assertThat(bashSeries.getMetric(), is("test.bash.stats"));
            assertThat(bashSeries.getTimestamps().size(), is(5));
            assertThat(bashSeries.getTimestamps().get(0), is(1475754111000L));
            assertThat(bashSeries.getTimestamps().get(1), is(1475754112000L));
            assertThat(bashSeries.getTimestamps().get(2), is(1475754113000L));
            assertThat(bashSeries.getTimestamps().get(3), is(1475754114000L));
            assertThat(bashSeries.getTimestamps().get(4), is(1475754115000L));

            assertThat(bashSeries.getValues().size(), is(5));
            assertThat(bashSeries.getValues().get(0), is(1.0));
            assertThat(bashSeries.getValues().get(1), is(2.0));
            assertThat(bashSeries.getValues().get(2), is(3.0));
            assertThat(bashSeries.getValues().get(3), is(4.0));
            assertThat(bashSeries.getValues().get(4), is(5.0));

            // test.ps.stats has 4 timestamps with 4 values
            MetricTimeSeries psSeries = series.get(1);
            assertThat(psSeries.getMetric(), is("test.ps.stats"));
            assertThat(psSeries.getTimestamps().size(), is(4));
            assertThat(psSeries.getTimestamps().get(0), is(1475754116000L));
            assertThat(psSeries.getTimestamps().get(1), is(1475754117000L));
            assertThat(psSeries.getTimestamps().get(2), is(1475754118000L));
            assertThat(psSeries.getTimestamps().get(3), is(1475754119000L));

            assertThat(psSeries.getValues().size(), is(4));
            assertThat(psSeries.getValues().get(0), is(6.0));
            assertThat(psSeries.getValues().get(1), is(7.0));
            assertThat(psSeries.getValues().get(2), is(8.0));
            assertThat(psSeries.getValues().get(3), is(9.0));
        }
    }
}