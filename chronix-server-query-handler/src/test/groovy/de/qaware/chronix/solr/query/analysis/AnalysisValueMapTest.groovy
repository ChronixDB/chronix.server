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
import spock.lang.Specification

/**
 * Unit test for the analysis value map
 * @author f.lautenschlager
 */
class AnalysisValueMapTest extends Specification {

    def "test analysis value map"() {
        given:
        def analysisValueMap = new AnalysisValueMap(size)

        when:
        size.times {
            analysisValueMap.add(new Max(), it, "Info:" + it)
        }

        then:
        analysisValueMap.size() == size
        size.times {
            analysisValueMap.getAnalysis(it) == new Max()
            analysisValueMap.getValue(it) == it
            analysisValueMap.getIdentifier(it) == "Info:" + it
        }

        where:
        size << [3]

    }

}
