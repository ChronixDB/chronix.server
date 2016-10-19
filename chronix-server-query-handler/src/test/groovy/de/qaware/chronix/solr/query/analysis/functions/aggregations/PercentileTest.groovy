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
 * Unit test for the percentile aggregation
 * @author f.lautenschlager
 */
class PercentileTest extends Specification {
    def "test execute"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("P");
        10.times {
            timeSeries.point(it, it * 10)
        }
        timeSeries.point(11, 9999)
        MetricTimeSeries ts = timeSeries.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);
        when:
        new Percentile(0.5).execute(ts, analysisResult)
        then:
        analysisResult.getAggregationValue(0) == 50.0d
    }

    def "test for empty time series"() {
        given:
        def analysisResult = new FunctionValueMap(1, 1, 1);
        when:
        new Percentile(0.5).execute(new MetricTimeSeries.Builder("Empty").build(), analysisResult)
        then:
        analysisResult.getAggregationValue(0) == Double.NaN
    }


    def "test arguments"() {
        expect:
        new Percentile(0.5).getArguments().size() == 1
    }

    def "test type"() {
        expect:
        new Percentile(0.5).getType() == FunctionType.P
    }

    def "test equals and hash code"() {
        when:
        def equals = p1.equals(p2)
        def p1Hash = p1.hashCode()
        def p2Hash = p2.hashCode()

        then:
        p1.equals(p1)
        !p1.equals(new Object())
        !p1.equals(null)
        equals == result
        p1Hash == p2Hash == result

        where:
        p1 << [new Percentile(0.1), new Percentile(0.2)]
        p2 << [new Percentile(0.1), new Percentile(0.1)]

        result << [true, false]
    }

    def "test to string"() {
        when:
        def stringRepresentation = new Percentile(0.2).toString()
        then:
        stringRepresentation.contains("0.2")
    }

}
