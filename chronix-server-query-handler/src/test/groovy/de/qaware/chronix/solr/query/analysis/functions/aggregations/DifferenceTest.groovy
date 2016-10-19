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
 * Unit test for the difference aggregation
 * @author f.lautenschlager
 */
class DifferenceTest extends Specification {
    def "test execute"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Difference");
        10.times {
            timeSeries.point(it + 1, it * 10)
        }
        timeSeries.point(0, -1)
        MetricTimeSeries ts = timeSeries.build()

        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new Difference().execute(ts, analysisResult)
        then:
        analysisResult.getAggregationValue(0) == 91d
    }

    def "test execute with negative values"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Difference");
        10.times {
            timeSeries.point(it + 1, (it + 1) * -10)
        }
        MetricTimeSeries ts = timeSeries.build()

        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new Difference().execute(ts, analysisResult)
        then:
        analysisResult.getAggregationValue(0) == 90d
    }


    def "test for empty time series"() {
        given:
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new Difference().execute(new MetricTimeSeries.Builder("Empty").build(), analysisResult)
        then:
        analysisResult.getAggregationValue(0) == Double.NaN
    }

    def "test arguments"() {
        expect:
        new Difference().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new Difference().getType() == FunctionType.DIFF
    }

    def "test equals and hash code"() {
        expect:
        def diff = new Difference();
        !diff.equals(null)
        !diff.equals(new Object())
        diff.equals(diff)
        diff.equals(new Difference())
        new Difference().hashCode() == new Difference().hashCode()
    }
}
