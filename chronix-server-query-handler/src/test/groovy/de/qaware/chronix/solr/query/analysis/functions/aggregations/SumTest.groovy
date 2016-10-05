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

import de.qaware.chronix.solr.query.analysis.FunctionValueMap
import de.qaware.chronix.solr.query.analysis.functions.FunctionType
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

/**
 * Unit test for the sum aggregation
 * @author f.lautenschlager
 */
class SumTest extends Specification {

    def "test execute"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Sum");
        10.times {
            timeSeries.point(it, it * 10)
        }
        timeSeries.point(11, 9999)
        MetricTimeSeries ts = timeSeries.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new Sum().execute(ts, analysisResult)
        then:
        analysisResult.getAggregationValue(0) == 10449.0d
    }


    def "test for empty time series"() {
        given:
        def analysisResult = new FunctionValueMap(1, 1, 1);
        when:
        new Sum().execute(new MetricTimeSeries.Builder("Empty").build(), analysisResult)
        then:
        analysisResult.getAggregationValue(0) == Double.NaN
    }

    def "test arguments"() {
        expect:
        new Sum().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new Sum().getType() == FunctionType.SUM
    }

    def "test equals and hash code"() {
        expect:
        def sum = new Sum();
        !sum.equals(null)
        !sum.equals(new Object())
        sum.equals(sum)
        sum.equals(new Sum())
        new Sum().hashCode() == new Sum().hashCode()
    }
}
