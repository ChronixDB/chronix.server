/*
 * Copyright (C) 2015 QAware GmbH
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
package de.qaware.chronix.solr.query.analysis.aggregation.aggregator

import spock.lang.Specification

import java.lang.reflect.MalformedParametersException
/**
 * @author f.lautenschlager
 */
class AggregationQueryEvaluatorTest extends Specification {

    def "test ag query strings"() {
        when:
        Map.Entry<AggregationType, Double> aggregation = AggregationQueryEvaluator.buildAggregation(fqs)
        then:
        aggregation.key == expectedAggreation
        aggregation.value == expectedValue
        where:
        fqs << [["ag=min"] as String[],
                ["ag=max"] as String[],
                ["ag=avg"] as String[],
                ["ag=dev"] as String[],
                ["ag=p=0.4"] as String[]]

        expectedAggreation << [AggregationType.MIN, AggregationType.MAX, AggregationType.AVG, AggregationType.DEV, AggregationType.P]
        expectedValue << [0, 0, 0, 0, 0.4]
    }

    def "test ag query strings that produce exceptions"() {
        when:
        AggregationQueryEvaluator.buildAggregation(fqs)

        then:
        thrown MalformedParametersException

        where:
        fqs << [["min"] as String[],
                ["ag=p=NotANumber"] as String[],
                ["ag=p="] as String[],
                null]

    }

    def "test private constructor"() {
        when:
        AggregationQueryEvaluator.newInstance()

        then:
        noExceptionThrown()
    }

}
