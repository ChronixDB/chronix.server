package de.qaware.chronix.solr.query.analysis.functions.aggregations

import de.qaware.chronix.solr.query.analysis.functions.AnalysisType
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

        when:
        def result = new Last().execute(timeSeries.build())

        then:
        result == 0d
    }

    def "test for empty time series"() {
        when:
        def result = new Last().execute([new MetricTimeSeries.Builder("Empty").build()] as MetricTimeSeries[])
        then:
        result == Double.NaN
    }


    def "test exception behaviour"() {
        when:
        new Last().execute(new MetricTimeSeries[0])
        then:
        thrown IllegalArgumentException.class
    }

    def "test subquery"() {
        expect:
        !new Last().needSubquery()
        new Last().getSubquery() == null
    }

    def "test arguments"() {
        expect:
        new Last().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new Last().getType() == AnalysisType.LAST
    }
}
