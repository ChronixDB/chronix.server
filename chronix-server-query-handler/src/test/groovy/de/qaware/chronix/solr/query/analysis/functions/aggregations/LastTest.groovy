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
 * Unit test for the last function
 * @author f.lautenschlager
 */
class LastTest extends Specification {

    def "test get last value"() {

        given:
        def timeSeries = new MetricTimeSeries.Builder("Last-Time-Series")

        10.times {
            timeSeries.point(10 - it, it)
        }
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new Last().execute(timeSeries.build(), analysisResult)

        then:
        analysisResult.getAggregationValue(0) == 0d
    }

    def "test for empty time series"() {
        given:
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new Last().execute(new MetricTimeSeries.Builder("Empty").build(), analysisResult)
        then:
        analysisResult.getAggregationValue(0) == Double.NaN
    }


    def "test arguments"() {
        expect:
        new Last().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new Last().getType() == FunctionType.LAST
    }

    def "test equals and hash code"() {
        expect:
        def last = new Last();
        !last.equals(null)
        !last.equals(new Object())
        last.equals(last)
        last.equals(new Last())
        new Last().hashCode() == new Last().hashCode()
    }
}
