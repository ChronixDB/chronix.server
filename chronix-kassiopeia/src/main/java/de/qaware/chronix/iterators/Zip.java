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

import static de.qaware.chronix.iterators.Iterators.all;
import static de.qaware.chronix.iterators.Iterators.any;


/**
 * This class zips a list of n iterators: Zipping n iterators gives an iterator
 * yielding at each call of next the list of the heads.
 * <p/>
 * Zipping can be strong or weak: Strong zipping stops at the first
 * exhausted iterator, weak at the last one, filling missing elements with null.
 *
 * @param <T> any type
 * @author johannes.siedersleben
 */
class Zip<T> implements ImmutableIterator<List<T>> {

    private final List<Iterator<T>> iterators;
    private final boolean weak;

    /**
     * @param iterators to be zipped
     * @param weak      true iff the resulting iterator stops at the last exhausted input iterator
     */
    public Zip(List<Iterator<T>> iterators, boolean weak) {
        this.iterators = iterators;
        this.weak = weak;
    }

    /**
     * @return true iff there is a next element according to the value of weak
     */
    public boolean hasNext() {
        return weak ? any(iterators, Iterator::hasNext) : all(iterators, Iterator::hasNext);
    }

    /**
     * @return the next element
     */
    public List<T> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        List<T> result = new ArrayList<>();
        for (Iterator<? extends T> t : iterators) {
            result.add(t.hasNext() ? t.next() : null);
        }
        return result;
    }
}
