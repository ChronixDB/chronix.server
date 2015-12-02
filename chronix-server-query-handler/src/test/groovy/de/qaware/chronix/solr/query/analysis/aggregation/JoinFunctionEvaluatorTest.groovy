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
package de.qaware.chronix.solr.query.analysis.aggregation

import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import spock.lang.Specification

/**
 * Unit test for the join function evaluator
 * @author f.lautenschlager
 */
class JoinFunctionEvaluatorTest extends Specification {
    def "test join function"() {
        given:
        def doc = new Document()
        doc.add(new StringField("host", "laptop", Field.Store.NO))
        doc.add(new StringField("source", "groovy", Field.Store.NO))
        doc.add(new StringField("metric", "unitTest", Field.Store.NO))


        when:
        def joinFunction = JoinFunctionEvaluator.joinFunction(filterQueries)
        def joinKey = joinFunction.apply(doc)
        then:
        joinKey == result

        where:
        filterQueries << [validJoinFilterQuery(), noJoinFilterQuery(), noFilterQueries(), null]
        result << ["laptop-unitTest-groovy", "unitTest", "unitTest", "unitTest"]
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
