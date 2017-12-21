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
package de.qaware.chronix.solr.query.analysis.providers

import spock.lang.Specification

/**
 * A unit test that is only used to increase test coverage.
 * The solr doc list provider uses a solr class that is tests
 * @author f.lautenschlager
 */
class SolrDocListProviderTest extends Specification {

    def "test doSimpleQuery"() {
        when:
        new SolrDocListProvider().doSimpleQuery("", null, 0, 99);

        then:
        thrown NullPointerException
    }

    def "test "() {
        when:
        new SolrDocListProvider().docListToSolrDocumentList(null, null, null, null);
        then:
        thrown NullPointerException
    }
}
