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

import de.qaware.chronix.solr.type.metric.MetricType
import de.qaware.chronix.solr.type.metric.functions.aggregations.Max
import de.qaware.chronix.solr.type.metric.functions.aggregations.Min
import spock.lang.Specification

class CQLCFResultTest extends Specification {

    CQLCFResult cqlResult
    MetricType metricType
    ChronixFunctions queryFunctions

    def setup() {
        cqlResult = new CQLCFResult()
        queryFunctions = new ChronixFunctions()
        metricType = new MetricType()
    }

    def "test add chronix functions for type"() {
        given:
        queryFunctions.addAggregation(new Max())

        when:
        cqlResult.addChronixFunctionsForType(metricType, queryFunctions)

        then:
        def gotQueryFunctions = cqlResult.getChronixFunctionsForType(metricType)
        gotQueryFunctions.size() == 1
        gotQueryFunctions.getAggregations().contains(new Max())
    }

    def "test add more chronix functions for type"() {
        given:
        queryFunctions.addAggregation(new Max())

        when:
        cqlResult.addChronixFunctionsForType(metricType, queryFunctions)

        and:
        queryFunctions.addAggregation(new Min())
        cqlResult.addChronixFunctionsForType(metricType, queryFunctions)

        then:
        def gotQueryFunctions = cqlResult.getChronixFunctionsForType(metricType)
        gotQueryFunctions.size() == 2
        gotQueryFunctions.getAggregations().contains(new Max())
        gotQueryFunctions.getAggregations().contains(new Min())
    }

    def "test is empty = true"() {
        expect:
        cqlResult.isEmpty()
    }

    def "test is empty = false"() {
        when:
        cqlResult.addChronixFunctionsForType(new MetricType(), new ChronixFunctions())
        then:
        !cqlResult.isEmpty()
    }
}
