/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis.functions

import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

/**
 * Unit test for the maximum aggregation
 * @author f.lautenschlager
 */
class MaxTest extends Specification {
    def "test execute"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Max");
        10.times {
            timeSeries.point(it, it * 10)
        }
        timeSeries.point(11, 9999)
        MetricTimeSeries ts = timeSeries.build()


        when:
        def result = new Max().execute(ts)
        then:
        result == 9999.0
    }

    def "test empty time series"() {
        when:
        def result = new Max().execute(new MetricTimeSeries.Builder("Max").build())
        then:
        result == 0.0
    }


    def "test exception behaviour"() {
        when:
        new Max().execute(new MetricTimeSeries[0])
        then:
        thrown IllegalArgumentException.class
    }

    def "test subquery"() {
        expect:
        !new Max().needSubquery()
        new Max().getSubquery() == null
    }

    def "test arguments"() {
        expect:
        new Max().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new Max().getType() == AnalysisType.MAX
    }
}
