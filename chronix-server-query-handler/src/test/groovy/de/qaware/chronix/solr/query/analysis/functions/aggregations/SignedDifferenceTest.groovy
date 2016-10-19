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
 * Unit test for the signed difference
 * @author f.lautenschlager
 */
class SignedDifferenceTest extends Specification {
    def "test execute with negative values"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Signed Difference");
        timeSeries.point(0, -1)
        timeSeries.point(1, -10)
        MetricTimeSeries ts = timeSeries.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new SignedDifference().execute(ts, analysisResult)
        then:
        analysisResult.getAggregationValue(0) == -9d
    }

    def "test execute with positive values"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Signed Difference");
        timeSeries.point(0, 1)
        timeSeries.point(1, 10)
        MetricTimeSeries ts = timeSeries.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new SignedDifference().execute(ts, analysisResult)
        then:
        analysisResult.getAggregationValue(0) == 9d
    }

    def "test execute with negative start and positive end"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Signed Difference");
        timeSeries.point(0, -1)
        timeSeries.point(1, 10)
        MetricTimeSeries ts = timeSeries.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new SignedDifference().execute(ts, analysisResult)
        then:
        analysisResult.getAggregationValue(0) == 11d
    }

    def "test execute with positive start and negative end"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Signed Difference");
        timeSeries.point(0, 1)
        timeSeries.point(1, -10)
        MetricTimeSeries ts = timeSeries.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);
        when:
        new SignedDifference().execute(ts, analysisResult)
        then:
        analysisResult.getAggregationValue(0) == -11d
    }


    def "test for empty time series"() {
        given:
        def analysisResult = new FunctionValueMap(1, 1, 1);
        when:
        new SignedDifference().execute(new MetricTimeSeries.Builder("Empty").build(), analysisResult)
        then:
        analysisResult.getAggregationValue(0) == Double.NaN
    }


    def "test arguments"() {
        expect:
        new SignedDifference().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new SignedDifference().getType() == FunctionType.SDIFF
    }

    def "test equals and hash code"() {
        expect:
        def sdiff = new SignedDifference();
        !sdiff.equals(null)
        !sdiff.equals(new Object())
        sdiff.equals(sdiff)
        sdiff.equals(new SignedDifference())
        new SignedDifference().hashCode() == new SignedDifference().hashCode()
    }
}
