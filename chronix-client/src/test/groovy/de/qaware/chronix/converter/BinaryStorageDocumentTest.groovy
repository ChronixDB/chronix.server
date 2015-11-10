/*
 *    Copyright (C) 2015 QAware GmbH
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
package de.qaware.chronix.converter

import spock.lang.Specification

import java.time.Instant

/**
 * Tests the creation of a binary storage document
 * @author f.lautenschlager
 */
class BinaryStorageDocumentTest extends Specification {

    def "test creation of a binary storage document"() {

        given:
        def builder = new BinaryStorageDocument.Builder();
        def start = Instant.now().toEpochMilli()
        def end = Instant.now().plusSeconds(64000).toEpochMilli()

        when:
        def binaryDocument = builder
                .id("6525-9662-2342")
                .start(start)
                .end(end)
                .data("The-Binary-Large-Object".getBytes())
                .field("host", "myProductionHost")
                .field("size", 0)
                .build()

        then:
        binaryDocument != null
        binaryDocument.getId() == "6525-9662-2342"
        binaryDocument.getStart() == start
        binaryDocument.getEnd() == end
        binaryDocument.getData() == "The-Binary-Large-Object".getBytes()
        binaryDocument.get("host") == "myProductionHost"
        binaryDocument.get("size") == 0

    }
}
