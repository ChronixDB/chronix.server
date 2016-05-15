package de.qaware.chronix.solr.query.analysis.functions.transformation

import de.qaware.chronix.solr.query.analysis.functions.ChronixTransformationType
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit test for the vector transformation
 * @author f.lautenschlager
 */
class VectorizationTest extends Specification {

    def "test transform"() {
        given:
        def timeSeriesBuilder = new MetricTimeSeries.Builder("Vector")
        def now = Instant.now()

        100.times {
            timeSeriesBuilder.point(now.plus(it, ChronoUnit.SECONDS).toEpochMilli(), it + 1)
        }

        def vectorization = new Vectorization();

        when:
        def vectorizedTimeSeries = vectorization.transform(timeSeriesBuilder.build())

        then:
        vectorizedTimeSeries.size() == 2
    }

    def "test type"() {
        when:
        def vectorization = new Vectorization();
        then:
        vectorization.getType() == ChronixTransformationType.VECTOR
    }
}
