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
package de.qaware.chronix.solr.query.analysis

import org.apache.solr.common.SolrDocument
import spock.lang.Specification

/**
 * Unit test for the join function QUERY_EVALUATOR
 * @author f.lautenschlager
 */
class JoinFunctionTest extends Specification {
    def "test join function"() {
        given:
        def doc = new SolrDocument()
        doc.addField("host", "laptop")
        doc.addField("source", "groovy")
        doc.addField("name", "unitTest")
        doc.addField("type", "metric")


        when:
        def joinFunction = new JoinFunction(joinOn)
        def joinKey = joinFunction.apply(doc)
        then:
        joinKey == result
        JoinFunction.isDefaultJoinFunction(joinFunction) == isDefault

        where:
        joinOn << ["host, name, source", "", null]
        result << ["laptop-unitTest-groovy", "unitTest-metric", "unitTest-metric"]
        isDefault << [false, true, true]
    }

    def "test private constructor"() {
        when:
        JoinFunction.newInstance()
        then:
        noExceptionThrown()
    }
}
