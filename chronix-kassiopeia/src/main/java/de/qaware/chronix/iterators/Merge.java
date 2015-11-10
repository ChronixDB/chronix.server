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
import java.util.NoSuchElementException;
import java.util.function.Function;

import static de.qaware.chronix.iterators.Iterators.all;
import static de.qaware.chronix.iterators.Iterators.any;


/**
 * This class merges n iterators as follows:
 * First step: return the list of the first element of each iterator and the index selected
 * by selector. A typical selector would chose the smallest or largest object.
 * Following steps: replace the selected element with its successor or null if there is none
 * and return the resulting list of objects.
 * Merge stops iff all input iterators are done (weak merge)
 * Strong merge would stop at the first exhausted input iterator.
 *
 * @param <T> any type
 * @author johannes.siedersleben
 */
class Merge<T> implements ImmutableIterator<T> {

    private final Function<List<T>, Integer> selector;
    private List<T> heads = new ArrayList<>();
    private List<Iterator<T>> myIterators = new ArrayList<>();
    private boolean weak;

    /**
     * @param iterators the iterators to be merged
     * @param selector  selects at each step the iterator where the next element will be taken
     * @param weak      if weak = false Merge stops at the first exhausted iterator,
     *                  if weak = true at the last one.
     */
    public Merge(List<Iterator<T>> iterators,
                 Function<List<T>, Integer> selector,
                 boolean weak) {

        this.weak = weak;
        this.selector = selector;
        for (Iterator<T> t : iterators) {
            heads.add(t.hasNext() ? t.next() : null);
            myIterators.add(t);
        }
    }

    /**
     * weak == true :there is a next element iff heads contains
     * at least of element not null
     * weak == false: there is a next element iff all elements of head
     * are not null
     *
     * @return true iff there is a next element
     */
    public boolean hasNext() {
        return weak ? any(heads, x -> x != null) : all(heads, x -> x != null);
    }

    /**
     * @return the next element
     */
    public T next() {

        // step of: determine nextIterator
        int i = selector.apply(heads);
        if (i == -1) {
            throw new NoSuchElementException();
        }

        // step two: prepare result
        T result = heads.get(i);

        // step three: update heads
        Iterator<T> t = myIterators.get(i);
        heads.set(i, t.hasNext() ? t.next() : null);

        return result;
    }
}
