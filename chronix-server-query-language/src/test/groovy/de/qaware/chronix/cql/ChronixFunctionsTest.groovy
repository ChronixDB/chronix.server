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

import de.qaware.chronix.solr.type.metric.functions.aggregations.Max
import de.qaware.chronix.solr.type.metric.functions.analyses.Trend
import de.qaware.chronix.solr.type.metric.functions.transformation.Vectorization
import spock.lang.Specification

/**
 * Unit test for the query functions
 * @author f.lautenschlager
 */
class ChronixFunctionsTest extends Specification {

    def "test query functions"() {
        given:
        def chronixFunctions = new ChronixFunctions()
        def vectorization = new Vectorization().setArguments(["0.01f"] as String[])

        when:
        chronixFunctions.addAggregation(new Max())
        chronixFunctions.addAnalysis(new Trend())
        chronixFunctions.addTransformation(vectorization)

        then:
        !chronixFunctions.isEmpty()
        chronixFunctions.size() == 3

        chronixFunctions.sizeOfAggregations() == 1
        chronixFunctions.sizeOfAnalyses() == 1
        chronixFunctions.sizeOfTransformations() == 1

        chronixFunctions.getAggregations().contains(new Max())
        chronixFunctions.getTransformations().contains(vectorization)
        chronixFunctions.getAnalyses().contains(new Trend())

        chronixFunctions.containsAggregations()
        chronixFunctions.containsAnalyses()
        chronixFunctions.containsTransformations()
    }

    def "test empty query functions"() {
        when:
        def chronixFunctions = new ChronixFunctions<>()

        then:
        chronixFunctions.isEmpty()
        chronixFunctions.size() == 0

        chronixFunctions.sizeOfAggregations() == 0
        chronixFunctions.sizeOfAnalyses() == 0
        chronixFunctions.sizeOfTransformations() == 0

        !chronixFunctions.getAggregations().contains(new Max())
        !chronixFunctions.getTransformations().contains(new Vectorization().setArguments(["0.01f"] as String[]))
        !chronixFunctions.getAnalyses().contains(new Trend())

        !chronixFunctions.containsAggregations()
        !chronixFunctions.containsAnalyses()
        !chronixFunctions.containsTransformations()
    }

}
