/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis.functions.analyses

import de.qaware.chronix.solr.query.analysis.functions.FunctionType
import de.qaware.chronix.solr.query.analysis.functions.FunctionValueMap
import de.qaware.chronix.timeseries.MetricTimeSeries
import org.apache.solr.common.util.Pair
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
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new FastDtw("", 5, 20).execute(new Pair(ts1, ts2), analysisResult)
        then:
        analysisResult.getAnalysisValue(0)
    }

    def "test time series with equal timestamps"() {
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("FastDTW-1");
        timeSeries.point(0, 2)
        3.times {
            timeSeries.point(1, it)
        }
        timeSeries.point(2, 2)

        def ts1 = timeSeries.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new FastDtw("", 5, 0).execute(new Pair(ts1, ts1), analysisResult)

        then:
        analysisResult.getAnalysisValue(0)

    }

    def "test execute for -1 as result"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("FastDTW-1");
        MetricTimeSeries.Builder secondTimeSeries = new MetricTimeSeries.Builder("FastDTW-2");
        10.times {
            timeSeries.point(it, it * 10)
            secondTimeSeries.point(it, it * -10)
        }
        def ts1 = timeSeries.build()
        def ts2 = secondTimeSeries.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new FastDtw("", 5, 0).execute(new Pair(ts1, ts2), analysisResult)
        then:
        !analysisResult.getAnalysisValue(0)
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
        new FastDtw("", 5, 20).getType() == FunctionType.FASTDTW
    }

    def "test equals and hash code"() {
        when:
        def equals = dtw1.equals(dtw2)
        def dtw1Hash = dtw1.hashCode()
        def dtw2Hash = dtw2.hashCode()

        then:
        dtw1.equals(dtw1)
        !dtw1.equals(new Object())
        !dtw1.equals(null)
        equals == result
        dtw1Hash == dtw2Hash == result

        where:
        dtw1 << [new FastDtw("", 5, 20), new FastDtw("metric:a", 5, 20), new FastDtw("metric:a", 5, 20), new FastDtw("metric:a", 5, 20)]
        dtw2 << [new FastDtw("", 5, 20), new FastDtw("metric:b", 5, 20), new FastDtw("metric:a", 6, 20), new FastDtw("metric:a", 5, 21)]

        result << [true, false, false, false]
    }

    def "test to string"() {
        when:
        def stringRepresentation = new FastDtw("metric:a", 5, 20).toString()
        then:
        stringRepresentation.contains("metric:a")
        stringRepresentation.contains("5")
        stringRepresentation.contains("20")
    }
}
