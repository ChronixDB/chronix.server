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
 * Unit test for the distinct transformation
 * @author f.lautenschlager
 */
class DistinctTest extends Specification {
    def "test transform"() {
        given:
        def timeSeriesBuilder = new MetricTimeSeries.Builder("Distinct")

        10.times {
            timeSeriesBuilder.point(it * 100, it + 10)
        }

        10.times {
            timeSeriesBuilder.point(it * 100 + 1, it + 10)
        }

        10.times {
            timeSeriesBuilder.point(it * 100 + 2, it + 10)
        }

        def timeSeries = timeSeriesBuilder.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);

        def distinct = new Distinct();
        when:
        distinct.execute(timeSeries, analysisResult)
        then:
        timeSeries.size() == 10
        timeSeries.getTime(0) == 0
        timeSeries.getValue(0) == 10

        analysisResult.getTransformation(0) == distinct
    }

    def "test getType"() {
        expect:
        new Distinct().getType() == FunctionType.DISTINCT
    }

    def "test equals and hash code"() {
        expect:
        def function = new Distinct();
        !function.equals(null)
        !function.equals(new Object())
        function.equals(function)
        function.equals(new Distinct())
        new Distinct().hashCode() == new Distinct().hashCode()
    }
}
