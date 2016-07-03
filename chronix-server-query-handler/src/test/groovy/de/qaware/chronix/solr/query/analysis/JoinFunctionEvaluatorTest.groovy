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

import org.apache.solr.common.SolrDocument
import spock.lang.Specification

/**
 * Unit test for the join function evaluator
 * @author f.lautenschlager
 */
class JoinFunctionEvaluatorTest extends Specification {
    def "test join function"() {
        given:
        def doc = new SolrDocument()
        doc.addField("host", "laptop")
        doc.addField("source", "groovy")
        doc.addField("metric", "unitTest")


        when:
        def joinFunction = JoinFunctionEvaluator.joinFunction(filterQueries)
        def joinKey = joinFunction.apply(doc)
        then:
        joinKey == result
        JoinFunctionEvaluator.isDefaultJoinFunction(joinFunction) == isDefault

        where:
        filterQueries << [validJoinFilterQuery(), noJoinFilterQuery(), noFilterQueries(), null]
        result << ["laptop-unitTest-groovy", "unitTest", "unitTest", "unitTest"]
        isDefault << [false, true, true, true]
    }

    private static String[] noFilterQueries() {
        []
    }

    private static String[] noJoinFilterQuery() {
        ["ag=max"]
    }

    private static String[] validJoinFilterQuery() {
        ["ag=max", "join=host,metric,source"]
    }

    def "test private constructor"() {
        when:
        JoinFunctionEvaluator.newInstance()
        then:
        noExceptionThrown()
    }
}
