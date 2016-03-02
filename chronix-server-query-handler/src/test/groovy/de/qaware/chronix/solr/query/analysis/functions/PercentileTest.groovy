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
 * Unit test for the percentile aggregation
 * @author f.lautenschlager
 */
class PercentileTest extends Specification {
    def "test execute"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("P");
        10.times {
            timeSeries.point(it, it * 10)
        }
        timeSeries.point(11, 9999)
        MetricTimeSeries ts = timeSeries.build()


        when:
        def result = new Percentile(0.5).execute(ts)
        then:
        result == 50.0
    }

    def "test exception behaviour"() {
        when:
        new Percentile(0.5).execute(new MetricTimeSeries[0])
        then:
        thrown IllegalArgumentException.class
    }

    def "test subquery"() {
        expect:
        !new Percentile(0.5).needSubquery()
        new Percentile(0.5).getSubquery() == null
    }

    def "test arguments"() {
        expect:
        new Percentile(0.5).getArguments().size() == 1
    }

    def "test type"() {
        expect:
        new Percentile(0.5).getType() == AnalysisType.P
    }

}
