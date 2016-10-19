/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis.functions.aggregations

import de.qaware.chronix.solr.query.analysis.functions.FunctionType
import de.qaware.chronix.solr.query.analysis.functions.FunctionValueMap
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

/**
 * Unit test for the standard deviation aggregation
 * @author f.lautenschlager
 */
class StdDevTest extends Specification {
    def "test execute"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Stddev");
        10.times {
            timeSeries.point(it, it * 10)
        }
        timeSeries.point(11, 9999)
        MetricTimeSeries ts = timeSeries.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new StdDev().execute(ts, analysisResult)
        then:
        analysisResult.getAggregationValue(0) == 3001.381363790528d
    }


    def "test for empty time series"() {
        given:
        def analysisResult = new FunctionValueMap(1, 1, 1);
        when:
        new StdDev().execute(new MetricTimeSeries.Builder("Empty").build(), analysisResult)
        then:
        analysisResult.getAggregationValue(0) == Double.NaN
    }

    def "test arguments"() {
        expect:
        new StdDev().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new StdDev().getType() == FunctionType.DEV
    }

    def "test equals and hash code"() {
        expect:
        def stdDev = new StdDev();
        !stdDev.equals(null)
        !stdDev.equals(new Object())
        stdDev.equals(stdDev)
        stdDev.equals(new StdDev())
        new StdDev().hashCode() == new StdDev().hashCode()
    }

}
