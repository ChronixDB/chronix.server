package de.qaware.chronix.solr.type.metric.functions.transformation

import spock.lang.Specification

class ResampleTest extends Specification {
    def "Execute"() {
    }

    def "GetQueryName"() {
        expect:
        new Resample().getQueryName() == "resample"
    }

    def "GetTimeSeriesType"() {
        expect:
        new Resample().getTimeSeriesType() == "metric"
    }
}
