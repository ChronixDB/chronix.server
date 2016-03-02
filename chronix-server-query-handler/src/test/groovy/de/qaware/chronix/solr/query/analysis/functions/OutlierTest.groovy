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
class OutlierTest extends Specification {
    def "test execute"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Out");
        10.times {
            timeSeries.point(it, it * 10)
        }
        timeSeries.point(11, 9999)
        MetricTimeSeries ts = timeSeries.build()


        when:
        def result = new Outlier().execute(ts)
        then:
        result == 1.0
    }

    def "test execute with a time series that has no outlier"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Out");
        10.times {
            timeSeries.point(it, 4711)
        }
        MetricTimeSeries ts = timeSeries.build()

        when:
        def result = new Outlier().execute(ts)
        then:
        result == -1.0
    }

    def "test execute with empty time series"() {
        when:
        def result = new Outlier().execute(new MetricTimeSeries.Builder("Out").build())
        then:
        result == -1.0
    }

    def "test exception behaviour"() {
        when:
        new Outlier().execute(new MetricTimeSeries[0])
        then:
        thrown IllegalArgumentException.class
    }

    def "test need subquery"() {
        expect:
        !new Outlier().needSubquery()
        new Outlier().getSubquery() == null
    }

    def "test arguments"() {
        expect:
        new Outlier().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new Outlier().getType() == AnalysisType.OUTLIER
    }

}
