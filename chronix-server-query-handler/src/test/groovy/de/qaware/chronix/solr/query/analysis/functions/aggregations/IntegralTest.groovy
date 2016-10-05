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
 * Unit test for the integral function
 * @author f.lautenschlager
 */
class IntegralTest extends Specification {
    def "test get last value"() {

        given:
        def timeSeries = new MetricTimeSeries.Builder("Integral")

        10.times {
            timeSeries.point(it + 1, it)
        }
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new Integral().execute(timeSeries.build(), analysisResult)

        then:
        analysisResult.getAggregationValue(0) == 36.000022888183594d
    }

    def "test for empty time series"() {
        given:
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new Integral().execute(new MetricTimeSeries.Builder("Empty").build(), analysisResult)
        then:
        analysisResult.getAggregationValue(0) == Double.NaN
    }


    def "test arguments"() {
        expect:
        new Integral().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new Integral().getType() == FunctionType.INTEGRAL
    }

    def "test equals and hash code"() {
        expect:
        def last = new Integral();
        !last.equals(null)
        !last.equals(new Object())
        last.equals(last)
        last.equals(new Integral())
        new Integral().hashCode() == new Integral().hashCode()
    }
}
