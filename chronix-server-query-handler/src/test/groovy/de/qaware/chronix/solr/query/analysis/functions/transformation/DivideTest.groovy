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

import de.qaware.chronix.solr.query.analysis.functions.FunctionType
import de.qaware.chronix.solr.query.analysis.functions.FunctionValueMap
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit test for the divide transformation
 * @author f.lautenschlager
 */
class DivideTest extends Specification {

    def "test transform"() {
        given:
        def timeSeriesBuilder = new MetricTimeSeries.Builder("Div")
        def now = Instant.now()

        100.times {
            timeSeriesBuilder.point(now.plus(it, ChronoUnit.SECONDS).toEpochMilli(), it + 1)
        }

        def divide = new Divide(2);
        def timeSeries = timeSeriesBuilder.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        divide.execute(timeSeries, analysisResult)

        then:
        100.times {
            timeSeries.getValue(it) == (it + 1) / 2d
        }
    }

    def "test getType"() {
        when:
        def divide = new Divide(2);
        then:
        divide.getType() == FunctionType.DIVIDE
    }

    def "test getArguments"() {
        when:
        def divide = new Divide(2);
        then:
        divide.getArguments()[0] == "value=2.0"
    }

    def "test equals and hash code"() {
        expect:
        def function = new Divide(4);
        !function.equals(null)
        !function.equals(new Object())
        function.equals(function)
        function.equals(new Divide(4))
        new Divide(4).hashCode() == new Divide(4).hashCode()
        new Divide(4).hashCode() != new Divide(2).hashCode()
    }
}
