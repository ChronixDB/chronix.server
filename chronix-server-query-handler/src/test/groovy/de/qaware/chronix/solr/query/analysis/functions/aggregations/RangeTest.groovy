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
 * Range analysis unit test
 * @author f.lautenschlager
 */
class RangeTest extends Specification {

    def "test range analysis"() {
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Range");
        10.times {
            timeSeries.point(it, it)
        }
        timeSeries.point(11, -5)

        MetricTimeSeries ts = timeSeries.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);
        when:
        new Range().execute(ts, analysisResult)
        then:
        analysisResult.getAggregationValue(0) == 14.0d

    }

    def "test for empty time series"() {
        given:
        def analysisResult = new FunctionValueMap(1, 1, 1);
        when:
        new Range().execute(new MetricTimeSeries.Builder("Empty").build(), analysisResult)
        then:
        analysisResult.getAggregationValue(0) == Double.NaN
    }


    def "test arguments"() {
        expect:
        new Range().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new Range().getType() == FunctionType.RANGE
    }

    def "test equals and hash code"() {
        expect:
        def range = new Range();
        !range.equals(null)
        !range.equals(new Object())
        range.equals(range)
        range.equals(new Range())
        new Range().hashCode() == new Range().hashCode()
    }

}
