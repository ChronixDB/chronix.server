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

import java.util.Iterator;
import java.util.function.Function;

import static de.qaware.chronix.dts.WeakLogic.weakEquals;

/**
 * This class implements an iterator which skips all consecutive duplicates
 * of its input.
 * Example: 1 1 2 2 2 3 3 4 produces 1 2 3 4.
 * But: 1 2 3 1 2 3 produces 1 2 3 1 2 3
 * Warning: an input iterator yielding infinitely many equal elements
 * such as (7 7 7 7 ...) causes an infinite loop.
 *
 * @param <T> type of iterator elements
 * @param <K> type of key iterator elements are mapped to
 * @author johannes.siedersleben
 */
class SkipDuplicates<T, K> implements ImmutableIterator<T> {

    private final Iterator<T> iterator;
    private final Function<? super T, ? extends K> key;
    private final boolean keepFirst;

    private boolean hasNext = false;
    private boolean itemLeft = false;           // indicates if there is a next item
    private T nextItem = null;                  // next item to be returned, may be null!

    /**
     * @param iterator  to be analysed
     * @param key       defining what duplicates are
     * @param keepFirst true iff the first duplicate will be kept
     */
    public SkipDuplicates(Iterator<T> iterator, Function<? super T, ? extends K> key, boolean keepFirst) {
        this.key = key;
        this.iterator = iterator;
        this.keepFirst = keepFirst;
        hasNext = iterator.hasNext();
        if (hasNext) {
            nextItem = iterator.next();
            itemLeft = true;
        }
    }

    /**
     * @return true iff there is a next element
     */
    public boolean hasNext() {
        return hasNext || itemLeft;
    }


    /**
     * @return the next element
     */
    public T next() {
        T result = nextItem;
        itemLeft = false;
        while (iterator.hasNext()) {
            T aux = iterator.next();   // result = first duplicate
            if (!weakEquals(key.apply(aux), key.apply(nextItem))) {
                nextItem = aux;
                itemLeft = true;
                break;
            } else if (!keepFirst) {   // result = last duplicate
                result = aux;
            }
        }

        hasNext = iterator.hasNext();
        return result;
    }
}
