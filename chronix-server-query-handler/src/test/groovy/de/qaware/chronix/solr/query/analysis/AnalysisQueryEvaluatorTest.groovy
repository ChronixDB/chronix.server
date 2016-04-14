/*
 * Copyright (C) 2016 QAware GmbH
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package de.qaware.chronix.solr.query.analysis

import de.qaware.chronix.solr.query.analysis.functions.AnalysisType
import spock.lang.Specification

/**
 * Unit test for the query evaluator class.
 * @author f.lautenschlager
 */
class AnalysisQueryEvaluatorTest extends Specification {

    def "test multiple queries"() {
        when:
        def aggregation = AnalysisQueryEvaluator.buildAnalyses(fqs)
        then:
        aggregation.size() == size

        where:
        fqs << [["ag=min;max;avg", "analysis=trend;outlier"] as String[],
                ["ag=min"] as String[]]

        size << [5, 1]
    }

    def "test query strings"() {
        when:
        def aggregation = AnalysisQueryEvaluator.buildAnalysis(fqs)
        then:
        aggregation.getType() == expectedAggreation
        aggregation.getArguments() == expectedValue
        aggregation.needSubquery() == needSubQuery
        aggregation.getSubquery() == subQuery
        where:
        fqs << [["ag=min"] as String[],
                ["ag=max"] as String[],
                ["ag=avg"] as String[],
                ["ag=dev"] as String[],
                ["ag=sum"] as String[],
                ["ag=count"] as String[],
                ["ag=p:0.4"] as String[],
                ["analysis=trend"] as String[],
                ["analysis=outlier"] as String[],
                ["analysis=frequency:10,6"] as String[],
                ["analysis=fastdtw:(metric:load* AND group:(A OR B)),5,0.4"] as String[],
                ["analysis=fastdtw:metric:load* AND group:(A OR B),5,0.4"] as String[]
        ]

        expectedAggreation << [AnalysisType.MIN, AnalysisType.MAX, AnalysisType.AVG, AnalysisType.DEV, AnalysisType.SUM, AnalysisType.COUNT, AnalysisType.P,
                               AnalysisType.TREND, AnalysisType.OUTLIER, AnalysisType.FREQUENCY, AnalysisType.FASTDTW, AnalysisType.FASTDTW]
        expectedValue << [new String[0], new String[0], new String[0], new String[0], new String[0], new String[0], ["percentile=0.4"] as String[],
                          new String[0], new String[0], ["window size=10", "window threshold=6"] as String[],
                          ["search radius=5", "max warping cost=0.4", "distance function=EUCLIDEAN"] as String[],
                          ["search radius=5", "max warping cost=0.4", "distance function=EUCLIDEAN"] as String[]]

        subQuery << [null, null, null, null, null, null, null, null, null, null, "metric:load* AND group:(A OR B)", "metric:load* AND group:(A OR B)"]
        needSubQuery << [false, false, false, false, false, false, false, false, false, false, true, true]
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
