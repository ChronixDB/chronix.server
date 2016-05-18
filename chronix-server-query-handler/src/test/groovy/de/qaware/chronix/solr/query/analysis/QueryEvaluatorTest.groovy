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

import de.qaware.chronix.solr.query.analysis.functions.FunctionType
import spock.lang.Specification

/**
 * Unit test for the query evaluator class.
 * @author f.lautenschlager
 */
class QueryEvaluatorTest extends Specification {

    def "test multiple queries"() {
        when:
        def queryFunctions = QueryEvaluator.extractFunctions(fqs)
        then:
        queryFunctions.size() == size

        where:
        fqs << [["function=min;max;avg", "function=trend;outlier"] as String[],
                ["function=min"] as String[]]

        size << [5, 1]
    }

    def "test aggregation query"() {
        when:
        def functions = QueryEvaluator.extractFunctions(fqs)
        then:
        def aggregation = functions.getAggregations().getAt(0)
        aggregation.getType() == expectedType
        aggregation.getArguments() == expectedArguments

        where:
        fqs << [["function=min"] as String[],
                ["function=max"] as String[],
                ["function=avg"] as String[],
                ["function=dev"] as String[],
                ["function=sum"] as String[],
                ["function=count"] as String[],
                ["function=first"] as String[],
                ["function=last"] as String[],
                ["function=range"] as String[],
                ["function=diff"] as String[],
                ["function=sdiff"] as String[],
                ["function=p:0.4"] as String[]
        ]

        expectedType << [FunctionType.MIN, FunctionType.MAX, FunctionType.AVG, FunctionType.DEV, FunctionType.SUM,
                         FunctionType.COUNT, FunctionType.FIRST, FunctionType.LAST, FunctionType.RANGE,
                         FunctionType.DIFF, FunctionType.SDIFF, FunctionType.P]
        expectedArguments << [new String[0], new String[0], new String[0], new String[0], new String[0], new String[0], new String[0],
                              new String[0], new String[0], new String[0], new String[0], ["percentile=0.4"] as String[]]
    }

    def "test analysis query"() {
        when:
        def functions = QueryEvaluator.extractFunctions(fqs)
        then:
        def analysis = functions.getAnalyses().getAt(0)
        analysis.getType() == expectedAggreation
        analysis.getArguments() == expectedValue
        analysis.needSubquery() == needSubQuery
        analysis.getSubquery() == subQuery
        where:
        fqs << [["function=trend"] as String[],
                ["function=outlier"] as String[],
                ["function=frequency:10,6"] as String[],
                ["function=fastdtw:(metric:load* AND group:(A OR B)),5,0.4"] as String[],
                ["function=fastdtw:metric:load* AND group:(A OR B),5,0.4"] as String[]
        ]

        expectedAggreation << [FunctionType.TREND, FunctionType.OUTLIER, FunctionType.FREQUENCY,
                               FunctionType.FASTDTW, FunctionType.FASTDTW]
        expectedValue << [new String[0], new String[0],
                          ["window size=10", "window threshold=6"] as String[],
                          ["search radius=5", "max warping cost=0.4", "distance function=EUCLIDEAN"] as String[],
                          ["search radius=5", "max warping cost=0.4", "distance function=EUCLIDEAN"] as String[]]

        subQuery << [null, null, null, "metric:load* AND group:(A OR B)", "metric:load* AND group:(A OR B)"]
        needSubQuery << [false, false, false, true, true]
    }

    def "test transformation query"() {
        when:
        def functions = QueryEvaluator.extractFunctions(fqs)
        then:
        def transformation = functions.getTransformations().getAt(0)
        transformation.getType() == expectedType

        where:
        fqs << [["function=vector"] as String[]]

        expectedType << [FunctionType.VECTOR]
    }

    def "test ag query strings that produce exceptions"() {
        when:
        QueryEvaluator.extractFunctions(fqs)

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
        QueryEvaluator.newInstance()

        then:
        noExceptionThrown()
    }

}
