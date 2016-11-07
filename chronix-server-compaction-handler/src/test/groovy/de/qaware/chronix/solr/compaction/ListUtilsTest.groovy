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
package de.qaware.chronix.solr.compaction

import de.qaware.chronix.converter.common.DoubleList
import de.qaware.chronix.converter.common.LongList
import spock.lang.Specification

import static de.qaware.chronix.solr.compaction.ListUtils.subList
import static de.qaware.chronix.solr.compaction.ListUtils.sublist

/**
 * Test case for {@link ListUtils}.
 *
 * @author alex.christ
 */
class ListUtilsTest extends Specification {
    def "test sublist for DoubleList"() {
        when:
        def list = new DoubleList([1, 2, 3] as double[], 3)

        then:
        subList(list, 0, 0).toArray() == [] as double[]
        subList(list, 0, 1).toArray() == [1] as double[]
        subList(list, 0, 2).toArray() == [1, 2] as double[]
        subList(list, 0, 3).toArray() == [1, 2, 3] as double[]
        subList(list, 1, 3).toArray() == [2, 3] as double[]
        subList(list, 2, 3).toArray() == [3] as double[]
        subList(list, 3, 3).toArray() == [] as double[]

        and:
        when:
        subList(list, -1, 2)

        then:
        thrown IndexOutOfBoundsException

        and:
        when:
        subList(list, 0, 4)

        then:
        thrown IndexOutOfBoundsException
    }

    def "test sublist for LongList"() {
        when:
        def list = new LongList([1, 2, 3] as long[], 3)

        then:
        sublist(list, 0, 0).toArray() == [] as long[]
        sublist(list, 0, 1).toArray() == [1] as long[]
        sublist(list, 0, 2).toArray() == [1, 2] as long[]
        sublist(list, 0, 3).toArray() == [1, 2, 3] as long[]
        sublist(list, 1, 3).toArray() == [2, 3] as long[]
        sublist(list, 2, 3).toArray() == [3] as long[]
        sublist(list, 3, 3).toArray() == [] as long[]

        and:
        when:
        sublist(list, -1, 2).toArray()
        then:
        thrown IndexOutOfBoundsException

        and:
        when:
        sublist(list, 0, 4).toArray()
        then:
        thrown IndexOutOfBoundsException

    }
}