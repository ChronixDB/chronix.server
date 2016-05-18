package de.qaware.chronix.solr.query.analysis.functions.transformation

import de.qaware.chronix.solr.query.analysis.functions.FunctionType
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit test for the moving average transformation
 * @author f.lautenschlager
 */
class MovingAverageTest extends Specification {
    def "test transform"() {
        given:
        def timeSeriesBuilder = new MetricTimeSeries.Builder("Div")
        def now = Instant.now()

        100.times {
            timeSeriesBuilder.point(now.plus(it, ChronoUnit.SECONDS).toEpochMilli(), it + 1)
        }
        def movAvg = new MovingAverage(4, ChronoUnit.SECONDS)

        when:
        def movingAvgSeries = movAvg.transform(timeSeriesBuilder.build())
        then:
        movingAvgSeries.size() == 25
    }

    def "test getType"() {
        when:
        def movAvg = new MovingAverage(4, ChronoUnit.DAYS)

        then:
        movAvg.getType() == FunctionType.MOVAVG
    }

    def "test getArguments"() {
        when:
        def movAvg = new MovingAverage(4, ChronoUnit.DAYS)

        then:
        movAvg.getArguments()[0] == "time span=4"
        movAvg.getArguments()[1] == "unit=4"
    }
}
