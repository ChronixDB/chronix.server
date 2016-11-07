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
package de.qaware.chronix.solr.compaction;

import de.qaware.chronix.converter.common.DoubleList;
import de.qaware.chronix.converter.common.LongList;

import static java.util.Arrays.copyOfRange;

/**
 * Utilities suplementing {@link DoubleList} and {@link LongList} with list interface methods.
 *
 * @author alex.christ
 */
public final class ListUtils {

    /**
     * No instantiation required.
     */
    private ListUtils() {

    }

    /**
     * @param list  the list
     * @param start the start index
     * @param end   the end index
     * @return sublist
     */
    public static LongList sublist(LongList list, int start, int end) {
        if (list.isEmpty()) {
            return list;
        }
        if (start < 0 || end > list.size()) {
            throw new IndexOutOfBoundsException("");
        }
        return new LongList(copyOfRange(list.toArray(), start, end), end - start);
    }

    /**
     * @param list  the list
     * @param start the start index
     * @param end   the end index
     * @return sublist
     */
    public static DoubleList subList(DoubleList list, int start, int end) {
        if (list.isEmpty()) {
            return list;
        }
        if (start < 0 || end > list.size()) {
            throw new IndexOutOfBoundsException("");
        }
        return new DoubleList(copyOfRange(list.toArray(), start, end), end - start);
    }
}