/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis.functions.analyses

import de.qaware.chronix.solr.query.analysis.FunctionValueMap
import de.qaware.chronix.solr.query.analysis.functions.FunctionType
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

/**
 * Unit test for the maximum aggregation
 * @author f.lautenschlager
 */
class OutlierTest extends Specification {
    def "test execute"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Out");
        10.times {
            timeSeries.point(it, it * 10)
        }
        timeSeries.point(11, 9999)
        MetricTimeSeries ts = timeSeries.build()

        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new Outlier().execute(ts, analysisResult)
        then:
        analysisResult.getAnalysisValue(0)
    }

    def "test execute with a time series that has no outlier"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Out");
        10.times {
            timeSeries.point(it, 4711)
        }
        MetricTimeSeries ts = timeSeries.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new Outlier().execute(ts, analysisResult)
        then:
        !analysisResult.getAggregationValue(0)
    }

    def "test execute with empty time series"() {
        given:
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new Outlier().execute(new MetricTimeSeries.Builder("Out").build(), analysisResult)
        then:
        !analysisResult.getAggregationValue(0)
    }

    def "test need subquery"() {
        expect:
        !new Outlier().needSubquery()
        new Outlier().getSubquery() == null
    }

    def "test arguments"() {
        expect:
        new Outlier().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new Outlier().getType() == FunctionType.OUTLIER
    }

    def "test equals and hash code"() {
        expect:
        def function = new Outlier();
        !function.equals(null)
        !function.equals(new Object())
        function.equals(function)
        function.equals(new Outlier())
        new Outlier().hashCode() == new Outlier().hashCode()
    }

}
