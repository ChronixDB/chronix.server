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
 * Unit test for the average aggregation
 * @author f.lautenschlager
 */
class AvgTest extends Specification {

    def "test execute"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Avg");
        10.times {
            timeSeries.point(it, it * 10)
        }
        timeSeries.point(11, 9999)
        MetricTimeSeries ts = timeSeries.build()

        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new Avg().execute(ts, analysisResult)
        then:
        analysisResult.getAggregationValue(0) == 949.9090909090909d
    }

    def "test for empty time series"() {
        given:
        def analysisResult = new FunctionValueMap(1, 1, 1);
        when:
        new Avg().execute(new MetricTimeSeries.Builder("Empty").build(), analysisResult)
        then:
        analysisResult.getAggregationValue(0) == Double.NaN;
    }


    def "test arguments"() {
        expect:
        new Avg().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new Avg().getType() == FunctionType.AVG
    }

    def "test equals and hash code"() {
        expect:
        def avg = new Avg();
        !avg.equals(null)
        !avg.equals(new Object())
        avg.equals(avg)
        avg.equals(new Avg())
        new Avg().hashCode() == new Avg().hashCode()
    }
}
