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

/**
 * Unit test for the compression class
 * @author f.lautenschlager
 */
class CompressionTest extends Specification {

    def "test compress and decompress"() {

        when:
        def compressed = Compression.compress(data)
        def decompressed = Compression.decompress(compressed);

        then:
        data == decompressed.bytes;

        where:
        data << ["Some Bytes".bytes]

    }

    def "test compression exception behaviour"() {
        when:
        def result = Compression.compress(null)
        def uncompressedResult = Compression.decompress(null)

        then:
        noExceptionThrown()
        result == null
        uncompressedResult == null;
    }

    def "test private constructor"() {
        when:
        Compression.newInstance()

        then:
        noExceptionThrown()
    }
}
