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

import spock.lang.Specification

class CQLTest extends Specification {


    def "test parse"() {
        given:
        def cql = new CQL();
        when:
        //CQL.CQLParseResult result = cql.parse("&cj=hans,rosa")
        CQL.CQLParseResult result = cql.parse("&cj=name,host,prozess&cf=metric{max;above:10,5m};trace{split}")

        then:
        result != null
    }
}
