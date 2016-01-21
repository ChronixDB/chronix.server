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
package de.qaware.chronix.solr.test.converter

import de.qaware.chronix.converter.BinaryTimeSeries
import spock.lang.Specification

/**
 * Simple test for the default document converter used in several tests
 * @author f.lautenschlager
 */
class DefaultTimeSeriesConverterTest extends Specification {
    def "test default document converter from and to"() {
        given:
        def binaryDocument = new BinaryTimeSeries.Builder()
                .id("4711-5896-7578")
                .build()
        def converter = new DefaultTimeSeriesConverter()

        when:
        def convertedDocument = converter.from(binaryDocument, 0, 0)
        def reconvertedDocument = converter.to(convertedDocument)

        then:
        reconvertedDocument.id == binaryDocument.id
    }

}
