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
class FirstTest extends Specification {

    def "test get last value"() {

        given:
        def timeSeries = new MetricTimeSeries.Builder("Last-Time-Series")
        def analysisResult = new FunctionValueMap(1, 1, 1);

        10.times {
            timeSeries.point(10 - it, it)
        }

        when:
        new First().execute(timeSeries.build(), analysisResult)

        then:
        analysisResult.getAggregationValue(0) == 9d
    }

    def "test for empty time series"() {
        given:
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new First().execute(new MetricTimeSeries.Builder("Empty").build(), analysisResult)
        then:
        analysisResult.getAggregationValue(0) == Double.NaN
    }


    def "test arguments"() {
        expect:
        new First().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new First().getType() == FunctionType.FIRST
    }

    def "test equals and hash code"() {
        expect:
        def first = new First();
        !first.equals(null)
        !first.equals(new Object())
        first.equals(first)
        first.equals(new First())
        new First().hashCode() == new First().hashCode()
    }
}
