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
package de.qaware.chronix.solr.type.metric.functions

import de.qaware.chronix.server.functions.FunctionCtx
import de.qaware.chronix.solr.type.metric.functions.aggregations.Max
import de.qaware.chronix.solr.type.metric.functions.analyses.Trend
import de.qaware.chronix.solr.type.metric.functions.filter.TopMetrics
import de.qaware.chronix.solr.type.metric.functions.transformation.Vectorization
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit test for the analysis value map
 * @author f.lautenschlager
 */
class FunctionCtxTest extends Specification {

    def "test analysis value map"() {
        given:
        def functionContext = new FunctionCtx(3, 3, 3, 3)

        when:
        aggregations.times {
            functionContext.add(new Max(), it as double, "my_join_key")
        }
        analyses.times {
            functionContext.add(new Trend(), true, "my_join_key")
        }

        transformations.times {
            functionContext.add(new Vectorization(), "my_join_key")
        }

        filters.times {
            functionContext.add(new TopMetrics(), "my_join_key")
        }


        then:

        def timeSeriesContext = functionContext.getContextFor("my_join_key")

        timeSeriesContext.size() == aggregations + analyses + transformations + filters

        timeSeriesContext.sizeOfAggregations() == aggregations
        timeSeriesContext.sizeOfAnalyses() == analyses
        timeSeriesContext.sizeOfTransformations() == transformations
        timeSeriesContext.sizeOfFilters() == filters

        timeSeriesContext.getAggregation(0) == new Max()
        timeSeriesContext.getAggregationValue(0) == 0

        timeSeriesContext.getAnalysis(0) == new Trend()
        timeSeriesContext.getAnalysisValue(0) == true

        timeSeriesContext.getTransformation(0) == new Vectorization()

        timeSeriesContext.getFilter(0) == new TopMetrics()

        where:
        aggregations << [3]
        analyses << [3]
        transformations << [3]
        filters << [3]
    }

    @Shared
    def functionValueMap = new FunctionCtx(0, 0, 0, 0)

    @Unroll
    def "test exception case for #function"() {
        when:
        function()

        then:
        thrown IndexOutOfBoundsException


        where:
        function << [{ -> functionValueMap.add(new Max(), 0.0d, "") },
                     { -> functionValueMap.add(new Trend(), true, "") },
                     { -> functionValueMap.add(new Vectorization(), "") },
                     { -> functionValueMap.add(new TopMetrics(), "")}]

    }
}
