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

import de.qaware.chronix.converter.dt.ProtocolBuffers
import de.qaware.chronix.dts.Pair
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit test for the protocol buffers converter
 *
 * @author f.lautenschlager
 */
class ProtocolBuffersConverterTest extends Specification {

    def "test private constructor"() {
        when:
        ProtocolBuffersConverter.newInstance()

        then:
        noExceptionThrown()
    }

    def "test to and from"() {
        given:
        def points = [Pair.pairOf(1L, 45d), Pair.pairOf(2L, 90d), Pair.pairOf(3L, 60d), Pair.pairOf(4L, 66d),]
        ProtocolBuffers.NumericPoints protoPoints = ProtocolBuffersConverter.to(points.iterator())
        def serializedPoints = new ByteArrayInputStream(protoPoints.toByteArray())

        when:
        def deserializedPoints = ProtocolBuffersConverter.from(serializedPoints, 1, 4);

        then:
        deserializedPoints.toList() == points
    }

    def "test to and from with range query"() {
        given:
        def points = [null, Pair.pairOf(null, 60d), Pair.pairOf(0L, null), Pair.pairOf(1L, 45d), Pair.pairOf(2L, 90d), Pair.pairOf(3L, 60d), Pair.pairOf(4L, 66d),]
        ProtocolBuffers.NumericPoints protoPoints = ProtocolBuffersConverter.to(points.iterator())
        def serializedPoints = new ByteArrayInputStream(protoPoints.toByteArray())

        when:
        def deserializedPoints = ProtocolBuffersConverter.from(serializedPoints, 1, 4, 2, 3);

        then:
        def resultingPoints = deserializedPoints.toList()

        resultingPoints.size() == 2
        resultingPoints.get(0).getFirst() == 2L
        resultingPoints.get(1).getFirst() == 3L
    }

    @Unroll
    def "test empty range queries: from '#from' to #to"() {

        given:
        def points = [Pair.pairOf(1L, 45d), Pair.pairOf(2L, 90d), Pair.pairOf(3L, 60d), Pair.pairOf(4L, 66d),]
        ProtocolBuffers.NumericPoints protoPoints = ProtocolBuffersConverter.to(points.iterator())
        def serializedPoints = new ByteArrayInputStream(protoPoints.toByteArray())

        when:
        def deserializedPoints = ProtocolBuffersConverter.from(serializedPoints, 1, 4, from, to);

        then:

        deserializedPoints.toList().size() == 0

        where:
        from << [0, 5, 5]
        to << [0, 1, 6]
    }

    def "test remove method - does nothing"() {
        given:
        def points = [Pair.pairOf(1L, 45d), Pair.pairOf(2L, 90d), Pair.pairOf(3L, 60d), Pair.pairOf(4L, 66d),]
        ProtocolBuffers.NumericPoints protoPoints = ProtocolBuffersConverter.to(points.iterator())
        def serializedPoints = new ByteArrayInputStream(protoPoints.toByteArray())

        when:
        def deserializedPoints = ProtocolBuffersConverter.from(serializedPoints, 1, 4);

        deserializedPoints.remove()

        then:
        deserializedPoints.toList() == points
    }

    def "test illegal arguments"() {
        when:
        ProtocolBuffersConverter.from(null, 1, 4, from, to)

        then:
        thrown IllegalArgumentException.class

        where:
        from << [-1, 0]
        to << [0, -1]
    }


    def "test exception during creation"() {
        when:
        ProtocolBuffersConverter.from(new ByteArrayInputStream("invalid_Bytes".getBytes("UTF-8")), 1, 4)

        then:
        thrown IllegalStateException.class
    }

    @Unroll
    def "test NoSuchElementException for query from '#from' to '#to' on time series from '1' to '4'"() {
        when:
        ProtocolBuffersConverter.from(null, 1, 4, from, to).next()

        then:
        thrown NoSuchElementException.class

        where:
        to << [0, 2, 3]
        from << [0, 5, 5]
    }

}
