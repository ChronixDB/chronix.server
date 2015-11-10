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
package de.qaware.chronix.iterators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static de.qaware.chronix.dts.WeakLogic.weakEquals;

/**
 * This class implements an iterator which groups elements by a key function:
 * consecutive elements with the same key value are grouped in lists.
 * The result is a Iterator of Lists.
 * The default key function is the identity.
 * Example A: x x x y y z produces ((x x x), (y y), (z))
 * Example B: 4 7 10 5 8 11 3, key(x) = x%3 produces ((4 7 10), (5 8 11), (3))
 * groupBy on an empty iterator yields an empty iterator
 *
 * @param <T> type of iterator elements
 * @param <V> type of values iterator elements are mapped to
 * @author johannes.siedersleben
 */
class GroupBy<T, V> implements ImmutableIterator<List<T>> {

    private final Function<T, V> myKey;
    private final Iterator<T> myIterator;

    private List<T> currentGroup = null;
    private List<T> nextGroup = new ArrayList<>();

    /**
     * Initialize instance with iterator and groupinbg function.
     *
     * @param t   the iterator
     * @param key the key
     */
    public GroupBy(Iterator<T> t, Function<T, V> key) {
        myIterator = t;
        myKey = key;
        prepareNext();
    }


    /**
     * @return true iff there is a next element
     */
    public boolean hasNext() {
        return nextGroup.size() + currentGroup.size() > 0;
    }

    /**
     * @return the next element
     */
    public List<T> next() {
        List<T> result = currentGroup;
        prepareNext();
        return result;
    }

    /**
     * This method computes currentGroup and nextGroup for the next call of hasNext
     * and next. It runs through myIterator until !hasNext or value of key changes.
     * It adds all matching elements to currentGroup and the first non-matching element
     * to nextGroup.
     */
    private void prepareNext() {
        currentGroup = nextGroup;
        nextGroup = new ArrayList<>();

        while (myIterator.hasNext()) {
            T nextElement = myIterator.next();
            int size = currentGroup.size();
            T lastElement = (size == 0) ? null : currentGroup.get(size - 1);

            if (size == 0 || weakEquals(myKey.apply(lastElement), (myKey.apply(nextElement)))) {
                currentGroup.add(nextElement);
            } else {
                // key has changed; currentGroup is up to date.
                // nextGroup contains the first element of the next group if any.
                nextGroup.add(nextElement);
                break;
            }
        }
    }
}
