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
 * Unit test for the value transformation
 * @author f.lautenschlager
 */
class ScaleTest extends Specification {

    def "test transform"() {
        given:
        def timeSeriesBuilder = new MetricTimeSeries.Builder("Scale")
        def now = Instant.now()

        100.times {
            timeSeriesBuilder.point(now.plus(it, ChronoUnit.SECONDS).toEpochMilli(), it + 1)
        }

        def scale = new Scale(2);
        def timeSeries = timeSeriesBuilder.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        scale.execute(timeSeries, analysisResult)

        then:
        100.times {
            timeSeries.getValue(it) == (it + 1) * 2
        }
    }

    def "test getType"() {
        when:
        def scale = new Scale(2);
        then:
        scale.getType() == FunctionType.SCALE
    }

    def "test getArguments"() {
        when:
        def scale = new Scale(2);
        then:
        scale.getArguments()[0] == "value=2.0"
    }

    def "test equals and hash code"() {
        expect:
        def function = new Scale(4);
        !function.equals(null)
        !function.equals(new Object())
        function.equals(function)
        function.equals(new Scale(4))
        new Scale(4).hashCode() == new Scale(4).hashCode()
        new Scale(4).hashCode() != new Scale(2).hashCode()
    }

    def "test string representation"() {
        expect:
        def string = new Scale(4).toString()
        string.contains("value")
    }
}
