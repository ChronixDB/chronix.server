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

import com.google.inject.Guice
import de.qaware.chronix.server.ChronixPluginLoader
import de.qaware.chronix.server.functions.plugin.ChronixFunctionPlugin
import de.qaware.chronix.server.types.ChronixTypePlugin
import de.qaware.chronix.solr.type.metric.MetricType
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit test for the query evaluator class.
 * @author f.lautenschlager
 */
class QueryEvaluatorTest extends Specification {

    @Shared
    QueryEvaluator evaluator

    def setup() {
        def injector = Guice.createInjector(
                ChronixPluginLoader.of(ChronixTypePlugin.class),
                ChronixPluginLoader.of(ChronixFunctionPlugin.class))
        evaluator = injector.getInstance(QueryEvaluator.class)
    }

    def "test plugins"() {
        when:
        def queryFunctions = evaluator.extractFunctions(fqs)
        then:
        queryFunctions.getTypeFunctions(new MetricType()).size() == size

        where:
        fqs << [["metric{nothing}"] as String[]]

        size << [0]
    }


    def "test multiple queries"() {
        when:
        def queryFunctions = evaluator.extractFunctions(fqs)
        then:
        queryFunctions.getTypeFunctions(new MetricType()).size() == size

        where:
        fqs << [["metric{min;max;avg}", "metric{trend;outlier}"] as String[],
                ["metric{min}"] as String[]]

        size << [5, 1]
    }

    def "test aggregation query"() {
        when:
        def functions = evaluator.extractFunctions(fqs)
        then:
        def aggregation = functions.getTypeFunctions(new MetricType()).getAggregations()[0]
        aggregation.getQueryName() == expectedType
        aggregation.getArguments() == expectedArguments

        where:
        fqs << [["metric{min}"] as String[],
                ["metric{max}"] as String[],
                ["metric{avg}"] as String[],
                ["metric{dev}"] as String[],
                ["metric{sum}"] as String[],
                ["metric{count}"] as String[],
                ["metric{first}"] as String[],
                ["metric{last}"] as String[],
                ["metric{range}"] as String[],
                ["metric{diff}"] as String[],
                ["metric{sdiff}"] as String[],
                ["metric{p:0.4}"] as String[],
                ["metric{integral}"] as String[]
        ]

        expectedType << ["min", "max", "avg", "dev", "sum",
                         "count", "first", "last", "range",
                         "diff", "sdiff", "p", "integral"]
        expectedArguments << [new String[0], new String[0], new String[0], new String[0], new String[0], new String[0], new String[0],
                              new String[0], new String[0], new String[0], new String[0], ["percentile=0.4"] as String[], new String[0]]
    }

    def "test analysis query"() {
        when:
        def functions = evaluator.extractFunctions(fqs)
        then:
        def analysis = functions.getTypeFunctions(new MetricType()).getAnalyses()[0]
        analysis.getQueryName() == expectedAggreation
        analysis.getArguments() == expectedValue
        analysis.needSubquery() == needSubQuery
        analysis.getSubquery() == subQuery
        where:
        fqs << [["metric{trend}"] as String[],
                ["metric{outlier}"] as String[],
                ["metric{frequency:10,6}"] as String[],
                ["metric{fastdtw:(metric:load* AND group:(A OR B)),5,0.4}"] as String[],
                ["metric{fastdtw:metric:load* AND group:(A OR B),5,0.4}"] as String[]
        ]

        expectedAggreation << ["trend", "outlier", "frequency",
                               "fastdtw", "fastdtw"]
        expectedValue << [new String[0], new String[0],
                          ["window size=10", "window threshold=6"] as String[],
                          ["search radius=5", "max warping cost=0.4", "distance function=EUCLIDEAN"] as String[],
                          ["search radius=5", "max warping cost=0.4", "distance function=EUCLIDEAN"] as String[]]

        subQuery << [null, null, null, "metric:load* and group:(a or b)", "metric:load* and group:(a or b)"]
        needSubQuery << [false, false, false, true, true]
    }


    @Unroll
    def "test transformation query #fqs"() {
        when:
        def functions = evaluator.extractFunctions(fqs)
        then:
        def transformation = functions.getTypeFunctions(new MetricType()).getTransformations()[0]
        transformation.getQueryName() == expectedType
        transformation.getArguments()[0] == expectedArgs

        where:
        fqs << [["metric{vector:0.01}"] as String[],
                ["metric{scale:4}"] as String[],
                ["metric{divide:4}"] as String[],
                ["metric{top:10}"] as String[],
                ["metric{bottom:10}"] as String[],
                ["metric{movavg:10,MINUTES}"] as String[],
                ["metric{add:10}"] as String[],
                ["metric{sub:10}"] as String[],
                ["metric{timeshift:10,SECONDS}"] as String[],
                ["metric{smovavg:10}"] as String[]
        ]

        expectedType << ["vector", "scale", "divide", "top",
                         "bottom", "movavg", "add", "sub",
                         "timeshift", "smovavg"]
        expectedArgs << ["tolerance=0.01", "value=4.0", "value=4.0", "value=10",
                         "value=10", "timeSpan=10", "value=10.0", "value=10.0",
                         "amount=10", "samples=10"]
    }

    @Unroll
    def "test transformation query without args #fqs"() {
        when:
        def functions = evaluator.extractFunctions(fqs)
        then:
        def transformation = functions.getTypeFunctions(new MetricType()).getTransformations()[0]
        transformation.getQueryName() == expectedType

        where:
        fqs << [["metric{derivative}"] as String[],
                ["metric{nnderivative}"] as String[],
                ["metric{distinct}"] as String[]]

        expectedType << ["derivative", "nnderivative", "distinct"]
    }

    //TODO: Fix.
    @Ignore
    def "test filter query strings that produce exceptions"() {
        when:
        evaluator.extractFunctions(fqs)

        then:
        thrown Exception

        where:
        fqs << [["metric{p=}"] as String[],
                ["metric{=}"] as String[],
                ["metric{UNKNOWN:127}"] as String[]]

    }

    def "test empty or null filter query"() {
        when:
        def result = evaluator.extractFunctions(fqs)

        then:
        noExceptionThrown()
        result.isEmpty()

        where:
        fqs << [[""] as String[], null]

    }

    def "test private constructor"() {
        when:
        QueryEvaluator.newInstance()

        then:
        noExceptionThrown()
    }

    def "test metric type extension"() {
        when:
        def functions = evaluator.extractFunctions(fqs)
        then:
        def aggregation = functions.getTypeFunctions(new MetricType()).getAggregations()[0]
        aggregation.getQueryName() == expectedType
        aggregation.getArguments() == expectedArguments

        where:
        fqs << [["metric{nonsense}"] as String[]]

        expectedType << ["nonsense"]
        expectedArguments << [new String[0]]
    }

}
