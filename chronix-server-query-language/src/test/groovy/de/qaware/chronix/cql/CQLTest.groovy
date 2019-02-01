/*
 * Copyright (C) 2018 QAware GmbH
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
package de.qaware.chronix.cql

import com.google.inject.Guice
import de.qaware.chronix.server.ChronixPluginLoader
import de.qaware.chronix.server.functions.plugin.ChronixFunctionPlugin
import de.qaware.chronix.server.functions.plugin.ChronixFunctions
import de.qaware.chronix.server.types.ChronixTypePlugin
import de.qaware.chronix.server.types.ChronixTypes
import de.qaware.chronix.solr.type.metric.MetricType
import de.qaware.chronix.solr.type.metric.functions.aggregations.Avg
import de.qaware.chronix.solr.type.metric.functions.aggregations.Max
import de.qaware.chronix.solr.type.metric.functions.aggregations.Min
import de.qaware.chronix.solr.type.metric.functions.analyses.Trend
import de.qaware.chronix.solr.type.metric.functions.transformation.Top
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class CQLTest extends Specification {


    @Shared
    ChronixTypes TYPES
    @Shared
    ChronixFunctions FUNCTIONS

    def setup() {
        def injector = Guice.createInjector(
                ChronixPluginLoader.of(ChronixTypePlugin.class),
                ChronixPluginLoader.of(ChronixFunctionPlugin.class))

        TYPES = injector.getInstance(ChronixTypes.class)
        FUNCTIONS = injector.getInstance(ChronixFunctions.class)
    }


    def "test parse chronix function (cf parameter)"() {
        given:
        def cql = new CQL(TYPES, FUNCTIONS)
        when:
        CQLCFResult result = cql.parseCF("metric{min;max;avg;top:10;trend}")

        then:
        def chronixFunctions = result.getChronixFunctionsForType(new MetricType())

        chronixFunctions.getAggregations().containsAll(new Min(), new Max(), new Avg())

        Top top = new Top()
        top.setArguments(["10"] as String[])
        chronixFunctions.getTransformations().containsAll(top)

        chronixFunctions.getAnalyses().containsAll(new Trend())
    }

    def "test parse chronix join (cj parameter)"() {
        given:
        def cql = new CQL(TYPES, FUNCTIONS)
        when:
        CQLJoinFunction joinFunction = cql.parseCJ("metric,type,name,host")

        then:
        joinFunction.involvedFields() == ["metric", "type", "name", "host"] as String[]
    }

    def "test metric type extension"() {
        given:
        def cql = new CQL(TYPES, FUNCTIONS)

        when:
        def functions = cql.parseCF("metric{noop}")

        then:
        def aggregation = functions.getChronixFunctionsForType(new MetricType()).getTransformations()[0]
        aggregation.getQueryName() == queryName
        aggregation.getArguments() == expectedArguments

        where:
        queryName << ["noop"]
        expectedArguments << [new String[0]]
    }

    @Unroll
    def "test cql queries that produce exceptions: #cf"() {
        given:
        def cql = new CQL(TYPES, FUNCTIONS)

        when:
        cql.parseCF(cf)

        then:
        thrown CQLException

        where:
        cf << ["metric{p=}",
               "metric{=}",
               "metric{UNKNOWN:127}"]

    }

    @Unroll
    def "test chronix functions of type transformation: #cf (without args)"() {
        given:
        def cql = new CQL(TYPES, FUNCTIONS)

        when:
        def functions = cql.parseCF(cf)
        then:
        def transformation = functions.getChronixFunctionsForType(new MetricType()).getTransformations()[0]
        transformation.getQueryName() == expectedQueryName

        where:
        cf << ["metric{derivative}",
               "metric{nnderivative}",
               "metric{distinct}"]

        expectedQueryName << ["derivative", "nnderivative", "distinct"]
    }

    @Unroll
    def "test chronix functions of type transformation: #cf"() {
        given:
        def cql = new CQL(TYPES, FUNCTIONS)

        when:
        def functions = cql.parseCF(cf)
        then:
        def transformation = functions.getChronixFunctionsForType(new MetricType()).getTransformations()[0]
        transformation.getQueryName() == expectedQueryName
        transformation.getArguments() == expectedArgs as String[]

        where:
        cf << ["metric{vector:0.01}",
               "metric{scale:4}",
               "metric{divide:4}",
               "metric{top:10}",
               "metric{bottom:10}",
               "metric{movavg:10,MINUTES}",
               "metric{add:10}",
               "metric{sub:10}",
               "metric{timeshift:10,SECONDS}",
               "metric{smovavg:10}"
        ]

        expectedQueryName << ["vector", "scale", "divide", "top",
                              "bottom", "movavg", "add", "sub",
                              "timeshift", "smovavg"]
        expectedArgs << [["tolerance=0.01"], ["value=4.0"], ["value=4.0"], ["value=10"],
                         ["value=10"], ["timeSpan=10", "unit=MINUTES"], ["value=10.0"], ["value=10.0"],
                         ["amount=10", "unit=SECONDS"], ["samples=10"]]
    }

    @Unroll
    def "test aggregation query #cf"() {
        given:
        def cql = new CQL(TYPES, FUNCTIONS)

        when:
        def functions = cql.parseCF(cf)
        then:
        def aggregation = functions.getChronixFunctionsForType(new MetricType()).getAggregations()[0]
        aggregation.getQueryName() == expectedQueryName
        aggregation.getArguments() == expectedArguments

        where:
        cf << ["metric{min}",
               "metric{max}",
               "metric{avg}",
               "metric{dev}",
               "metric{sum}",
               "metric{count}",
               "metric{first}",
               "metric{last}",
               "metric{range}",
               "metric{diff}",
               "metric{sdiff}",
               "metric{p:0.4}",
               "metric{integral}"
        ]

        expectedQueryName << ["min", "max", "avg", "dev", "sum",
                              "count", "first", "last", "range",
                              "diff", "sdiff", "p", "integral"]
        expectedArguments << [new String[0], new String[0], new String[0], new String[0], new String[0], new String[0], new String[0],
                              new String[0], new String[0], new String[0], new String[0], ["percentile=0.4"] as String[], new String[0]]
    }

    //Fix this: Implement fastdtw
    @Unroll
    def "test chronix function of type analysis: #cf"() {
        given:
        def cql = new CQL(TYPES, FUNCTIONS)

        when:
        def functions = cql.parseCF(cf)
        then:
        def analysis = functions.getChronixFunctionsForType(new MetricType()).getAnalyses()[0]
        analysis.getQueryName() == expectedQueryName
        analysis.getArguments() == expectedValue
        analysis.needSubquery() == needSubQuery
        analysis.getSubquery() == subQuery
        where:
        cf << ["metric{trend}",
               "metric{outlier}",
               "metric{frequency:10,6}"
               /*["metric{fastdtw:(metric:load* AND group:(A OR B)),5,0.4}"] as String[],
               ["metric{fastdtw:metric:load* AND group:(A OR B),5,0.4}"] as String[]*/
        ]

        expectedQueryName << ["trend", "outlier", "frequency"/*,
                              "fastdtw", "fastdtw"*/]
        expectedValue << [new String[0], new String[0],
                          ["window size=10", "window threshold=6"] as String[]/*,
                          ["search radius=5", "max warping cost=0.4", "distance function=EUCLIDEAN"] as String[],
                          ["search radius=5", "max warping cost=0.4", "distance function=EUCLIDEAN"] as String[]*/]

        subQuery << [null, null, null]//, "metric:load* AND group:(A OR B)", "metric:load* AND group:(A OR B)"]
        needSubQuery << [false, false, false]//, true, true]
    }
}
