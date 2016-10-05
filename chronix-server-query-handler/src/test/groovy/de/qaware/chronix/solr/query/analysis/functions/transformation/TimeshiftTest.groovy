/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis.functions.transformation

import de.qaware.chronix.solr.query.analysis.FunctionValueMap
import de.qaware.chronix.solr.query.analysis.functions.FunctionType
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

import java.time.temporal.ChronoUnit

/**
 * Unit test for the timeshift transformation
 * @author f.lautenschlager
 */
class TimeshiftTest extends Specification {
    def "test transform"() {
        given:
        def timeSeriesBuilder = new MetricTimeSeries.Builder("Timeshift")
        10.times {
            timeSeriesBuilder.point(it * 100, it + 10)
        }
        timeSeriesBuilder.point(10 * 100, -10)
        def timeSeries = timeSeriesBuilder.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);


        def timeshift = new Timeshift(4, ChronoUnit.MILLIS);

        when:
        timeshift.execute(timeSeries, analysisResult)
        then:
        timeSeries.size() == 11
        timeSeries.getTime(0) == 4
        timeSeries.getValue(0) == 10

        timeSeries.getTime(10) == 1004
        timeSeries.getValue(10) == -10
    }

    def "test transform for negative amount"() {
        given:
        def timeSeriesBuilder = new MetricTimeSeries.Builder("Timeshift")
        10.times {
            timeSeriesBuilder.point(it * 100, it + 10)
        }
        timeSeriesBuilder.point(10 * 100, -10)
        def timeSeries = timeSeriesBuilder.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);

        def timeshift = new Timeshift(-4, ChronoUnit.MILLIS);

        when:
        timeshift.execute(timeSeries,analysisResult)
        then:
        timeSeries.size() == 11
        timeSeries.getTime(0) == -4
        timeSeries.getValue(0) == 10

        timeSeries.getTime(10) == 996
        timeSeries.getValue(10) == -10
    }

    def "test getType"() {
        expect:
        new Timeshift(4, ChronoUnit.DAYS).getType() == FunctionType.TIMESHIFT
    }

    def "test getArguments"() {
        expect:
        new Timeshift(4, ChronoUnit.DAYS).getArguments()[0] == "amount=4"
        new Timeshift(4, ChronoUnit.DAYS).getArguments()[1] == "unit=DAYS"
    }

    def "test equals and hash code"() {
        expect:
        def function = new Timeshift(4, ChronoUnit.DAYS);
        !function.equals(null)
        !function.equals(new Object())
        function.equals(function)
        function.equals(new Timeshift(4, ChronoUnit.DAYS))
        new Timeshift(4, ChronoUnit.DAYS).hashCode() == new Timeshift(4, ChronoUnit.DAYS).hashCode()
        new Timeshift(4, ChronoUnit.DAYS).hashCode() != new Timeshift(2, ChronoUnit.DAYS).hashCode()
        new Timeshift(4, ChronoUnit.DAYS).hashCode() != new Timeshift(2, ChronoUnit.SECONDS).hashCode()
    }

    def "test string representation"() {
        expect:
        def string = new Timeshift(4, ChronoUnit.DAYS).toString()
        string.contains("unit")
        string.contains("amount")
    }
}
