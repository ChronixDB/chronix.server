/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis

import de.qaware.chronix.solr.query.analysis.functions.AnalysisType
import de.qaware.chronix.solr.query.analysis.functions.ChronixAnalysis
import spock.lang.Specification

/**
 * @author f.lautenschlager
 */
class AnalysisQueryEvaluatorTest extends Specification {

    def "test query strings"() {
        when:
        ChronixAnalysis aggregation = AnalysisQueryEvaluator.buildAnalysis(fqs)
        then:
        aggregation.getType() == expectedAggreation
        //aggregation.getArguments() == expectedValue
        where:
        fqs << [["ag=min"] as String[],
                ["ag=max"] as String[],
                ["ag=avg"] as String[],
                ["ag=dev"] as String[],
                ["ag=p:0.4"] as String[],
                ["analysis=trend"] as String[],
                ["analysis=outlier"] as String[],
                ["analysis=frequency:10,6"] as String[],
                ["analysis=fastdtw:(metric:load*),5,0.4"] as String[],
                ["analysis=sax:*af*,7,10,0.01"] as String[]
        ]

        expectedAggreation << [AnalysisType.MIN, AnalysisType.MAX, AnalysisType.AVG, AnalysisType.DEV, AnalysisType.P,
                               AnalysisType.TREND, AnalysisType.OUTLIER, AnalysisType.FREQUENCY, AnalysisType.FASTDTW, AnalysisType.SAX]
        expectedValue << [new String[0], new String[0], new String[0], new String[0], ["0.4"] as String[],
                          new String[0], new String[0], ["10", "6"] as String[], ["(metric:load*)", "5", "0.4"] as String[],
                          ["*af", "7,10,0.01"] as String[]]
    }

    def "test ag query strings that produce exceptions"() {
        when:
        AnalysisQueryEvaluator.buildAnalysis(fqs)

        then:
        thrown Exception

        where:
        fqs << [["min"] as String[],
                ["ag=p="] as String[],
                ["analysis"] as String[],
                ["analysis=UNKNOWN:127"] as String[],
                null]

    }

    def "test private constructor"() {
        when:
        AnalysisQueryEvaluator.newInstance()

        then:
        noExceptionThrown()
    }

}
