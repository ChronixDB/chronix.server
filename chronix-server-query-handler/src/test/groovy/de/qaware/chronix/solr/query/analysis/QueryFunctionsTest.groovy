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
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

/**
 * Unit test for the query functions
 * @author f.lautenschlager
 */
class QueryFunctionsTest extends Specification {

    def "test query functions"() {
        given:
        def queryFunctions = new QueryFunctions<MetricTimeSeries>();

        when:
        queryFunctions.addAggregation(new Max())
        queryFunctions.addAnalysis(new Trend())
        queryFunctions.addTransformation(new Vectorization(0.01f))

        then:
        !queryFunctions.isEmpty()
        queryFunctions.size() == 3

        queryFunctions.sizeOfAggregations() == 1
        queryFunctions.sizeOfAnalyses() == 1
        queryFunctions.sizeOfTransformations() == 1

        queryFunctions.getAggregations().contains(new Max())
        queryFunctions.getTransformations().contains(new Vectorization(0.01f))
        queryFunctions.getAnalyses().contains(new Trend())

        queryFunctions.containsAggregations()
        queryFunctions.containsAnalyses()
        queryFunctions.containsTransformations()
    }

    def "test empty query functions"() {
        when:
        def queryFunctions = new QueryFunctions<MetricTimeSeries>();

        then:
        queryFunctions.isEmpty()
        queryFunctions.size() == 0

        queryFunctions.sizeOfAggregations() == 0
        queryFunctions.sizeOfAnalyses() == 0
        queryFunctions.sizeOfTransformations() == 0

        !queryFunctions.getAggregations().contains(new Max())
        !queryFunctions.getTransformations().contains(new Vectorization(0.01f))
        !queryFunctions.getAnalyses().contains(new Trend())

        !queryFunctions.containsAggregations()
        !queryFunctions.containsAnalyses()
        !queryFunctions.containsTransformations()
    }

}
