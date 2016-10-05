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

/**
 * Unit test for the subtract transformation
 * @author f.lautenschlager
 */
class SubtractTest extends Specification {
    def "test transform"() {
        given:
        def timeSeriesBuilder = new MetricTimeSeries.Builder("Add")
        10.times {
            timeSeriesBuilder.point(it * 100, it + 10)
        }
        timeSeriesBuilder.point(10 * 100, -10)
        def timeSeries = timeSeriesBuilder.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);


        def sub = new Subtract(4);
        when:
        sub.execute(timeSeries, analysisResult)

        then:
        timeSeries.size() == 11
        timeSeries.getValue(1) == (1 + 10 - 4)

        timeSeries.getValue(10) == -14
    }

    def "test getType"() {
        expect:
        new Subtract(4).getType() == FunctionType.SUB
    }

    def "test getArguments"() {
        expect:
        new Subtract(4).getArguments()[0] == "value=4.0"
    }

    def "test equals and hash code"() {
        expect:
        def function = new Subtract(4);
        !function.equals(null)
        !function.equals(new Object())
        function.equals(function)
        function.equals(new Subtract(4))
        new Subtract(4).hashCode() == new Subtract(4).hashCode()
        new Subtract(4).hashCode() != new Subtract(2).hashCode()
    }

    def "test string representation"() {
        expect:
        def string = new Subtract(4).toString()
        string.contains("value")
    }
}
