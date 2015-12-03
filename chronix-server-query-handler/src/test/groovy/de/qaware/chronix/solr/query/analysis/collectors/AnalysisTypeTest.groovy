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
package de.qaware.chronix.solr.query.analysis.collectors

import spock.lang.Specification

/**
 * Unit test for the analysis type enum
 * @author f.lautenschlager
 */
class AnalysisTypeTest extends Specification {

    def "test analyses types"() {
        when:
        def result = AnalysisType.isAggregation(type)

        then:
        result == expected

        where:
        type << [AnalysisType.MIN, AnalysisType.MAX, AnalysisType.AVG, AnalysisType.DEV, AnalysisType.P, AnalysisType.TREND, AnalysisType.OUTLIER, AnalysisType.FREQUENCY]
        expected << [true, true, true, true, true, false, false, false]
    }
}
