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
 * Unit test for the minimum aggregation
 * @author f.lautenschlager
 */
class MinTest extends Specification {
    def "test execute with negative"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Min");
        10.times {
            timeSeries.point(it, it * -10)
        }
        timeSeries.point(11, 9999)
        MetricTimeSeries ts = timeSeries.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);
        when:
        new Min().execute(ts, analysisResult)
        then:
        analysisResult.getAggregationValue(0) == -90.0d
    }

    def "test execute with positive number"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Min");
        10.times {
            timeSeries.point(it, it * 10 + 1)
        }
        timeSeries.point(11, 0)
        MetricTimeSeries ts = timeSeries.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new Min().execute(ts, analysisResult)
        then:
        analysisResult.getAggregationValue(0) == 0.0d
    }

    def "test for empty time series"() {
        given:
        def analysisResult = new FunctionValueMap(1, 1, 1);
        when:
        new Min().execute(new MetricTimeSeries.Builder("Empty").build(), analysisResult)
        then:
        analysisResult.getAggregationValue(0) == Double.NaN
    }


    def "test arguments"() {
        expect:
        new Min().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new Min().getType() == FunctionType.MIN
    }

    def "test equals and hash code"() {
        expect:
        def min = new Min();
        !min.equals(null)
        !min.equals(new Object())
        min.equals(min)
        min.equals(new Min())
        new Min().hashCode() == new Min().hashCode()
    }
}
