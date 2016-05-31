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

import de.qaware.chronix.solr.query.analysis.functions.aggregations.Max
import de.qaware.chronix.solr.query.analysis.functions.analyses.Trend
import de.qaware.chronix.solr.query.analysis.functions.transformation.Vectorization
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit test for the analysis value map
 * @author f.lautenschlager
 */
class FunctionValueMapTest extends Specification {

    def "test analysis value map"() {
        given:
        def functionValueMap = new FunctionValueMap(3, 3, 3)

        when:
        aggregations.times {
            functionValueMap.add(new Max(), it)
        }
        analyses.times {
            functionValueMap.add(new Trend(), true, "")
        }

        transformations.times {
            functionValueMap.add(new Vectorization(0.1f))
        }


        then:
        functionValueMap.size() == aggregations + analyses + transformations

        functionValueMap.sizeOfAggregations() == aggregations
        functionValueMap.sizeOfAnalyses() == analyses
        functionValueMap.sizeOfTransformations() == transformations

        functionValueMap.getAggregation(0) == new Max()
        functionValueMap.getAggregationValue(0) == 0

        functionValueMap.getAnalysis(0) == new Trend()
        functionValueMap.getAnalysisValue(0) == true
        functionValueMap.getAnalysisIdentifier(0) == ""

        functionValueMap.getTransformation(0) == new Vectorization(0.1f)

        where:
        aggregations << [3]
        analyses << [3]
        transformations << [3]
    }

    @Shared
    def functionValueMap = new FunctionValueMap(0, 0, 0)

    @Unroll
    def "test exception case for #function"() {
        when:
        function()

        then:
        thrown IndexOutOfBoundsException


        where:
        function << [{ -> functionValueMap.add(new Max(), 0.0) },
                     { -> functionValueMap.add(new Trend(), true, "") },
                     { -> functionValueMap.add(new Vectorization(0.1f)) }]

    }
}
