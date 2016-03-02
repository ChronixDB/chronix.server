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
 * Unit test for the fast dtw analysis
 * @author f.lautenschlager
 */
class FastDtwTest extends Specification {
    def "test execute"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("FastDTW");
        10.times {
            timeSeries.point(it, it + 10)
        }
        timeSeries.point(11, 9999)
        MetricTimeSeries ts1 = timeSeries.build()
        MetricTimeSeries ts2 = timeSeries.build()


        when:
        def result = new FastDtw("", 5, 20).execute(ts1, ts2)
        then:
        result == 0
    }

    def "test execute for with -1 as result"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("FastDTW-1");
        MetricTimeSeries.Builder secondTimeSeries = new MetricTimeSeries.Builder("FastDTW-2");
        10.times {
            timeSeries.point(it, it * 10)
            secondTimeSeries.point(it, it * -10)
        }
        def ts1 = timeSeries.build()
        def ts2 = secondTimeSeries.build()


        when:
        def result = new FastDtw("", 5, 0).execute(ts1, ts2)
        then:
        result == -1.0

    }

    def "test exception behaviour"() {
        when:
        new FastDtw("", 5, 20).execute(new MetricTimeSeries[0])
        then:
        thrown IllegalArgumentException.class
    }

    def "test subquery"() {
        expect:
        new FastDtw("", 5, 20).needSubquery()
        new FastDtw("", 5, 20).getArguments().size() == 3
        new FastDtw("(query)", 5, 20).getSubquery().equals("(query)")
    }

    def "test arguments"() {
        expect:
        new Outlier().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new FastDtw("", 5, 20).getType() == AnalysisType.FASTDTW
    }
}
