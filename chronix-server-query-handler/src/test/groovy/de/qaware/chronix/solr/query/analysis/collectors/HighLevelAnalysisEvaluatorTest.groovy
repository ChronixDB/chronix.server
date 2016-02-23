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
package de.qaware.chronix.solr.query.analysis.collectors

import spock.lang.Specification
/**
 * Unit test for the high level analysis evaluator
 * @author f.lautenschlager
 */
class HighLevelAnalysisEvaluatorTest extends Specification {

    def "Analyze"() {

    }

    def "Analyze1"() {

    }

    /*
    def "test high level analysis"() {
        given:
        def docs = fillDocs()
        def analysis = AnalysisQueryEvaluator.buildAnalysis(["analysis=trend"] as String[])

        when:
        def document = AnalysisDocumentBuilder.analyze(analysis, 1l, Long.MAX_VALUE, "groovy-laptops", docs);

        then:
        document.getFieldValue("host") as String == "laptop"
        document.getFieldValue("metric") as String == "groovy"
        document.getFieldValue("start") as long == 1
        document.getFieldValue("end") as long == 991
        document.getFieldValue("value") == null
        document.getFieldValue("analysis") as String == "TREND"
        document.getFieldValue("analysisParam") as String == ""
        document.getFieldValue("joinKey") as String == "groovy-laptop"
        document.getFieldValue("data") != null

        document.getFieldValue("someInt") as int == 1i
        document.getFieldValue("someFloat") as float == 1.1f
        document.getFieldValue("someDouble") as double == 2.0d

    }
    */
}
