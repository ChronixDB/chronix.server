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
package de.qaware.chronix.timeseries;


import de.qaware.chronix.dts.Pair;
import de.qaware.chronix.iterators.ImmutableIterator;

import java.util.*;

import static de.qaware.chronix.dts.WeakLogic.weakComparator;
import static de.qaware.chronix.iterators.Iterators.any;

/**
 * This class merges a list of time series. They are are given as iterators of (time, value)-pairs.
 * TimeMerge produces a new such iterator by unioning all input series. It may have identical
 * timestamps and/or identical subsequent values. It is to be used as input for the constructor of TimeSeries.
 *
 * @param <T> class of arguments. Must be comparable
 * @param <V> class of values. Can be any class.
 * @author johannes.siedersleben
 */
class TimeSeriesMerge<T extends Comparable, V> implements ImmutableIterator<Pair<T, List<V>>> {

    private final Comparator<T> cmp = weakComparator();         // compare by step (first element of pair)
    private List<Iterator<Pair<T, V>>> iterators = new ArrayList<>();   // iterators to be merged
    private List<Pair<T, V>> heads = new ArrayList<>();    // list of last entries read
    private List<V> values = new ArrayList<>();

    /**
     * @param iterators to be merged
     */
    public TimeSeriesMerge(Iterator<Iterator<Pair<T, V>>> iterators) {
        while (iterators.hasNext()) {
            Iterator<Pair<T, V>> t = iterators.next();
            this.iterators.add(t);
            heads.add(t.hasNext() ? t.next() : null);
            values.add(null);
        }
    }

    /**
     * @param iterators to be merged
     */
    public TimeSeriesMerge(Iterable<Iterator<Pair<T, V>>> iterators) {
        this(iterators.iterator());
    }

    // find iterator with minimal timestamp
    private int nextIterator() {
        Pair<T, V> minEntry = null;
        int result = -1;
        for (int i = 0; i < iterators.size(); i++) {
            Pair<T, V> currentEntry = heads.get(i);
            if (currentEntry == null) {
                continue;
            }
            if (minEntry == null || cmp.compare(currentEntry.getFirst(), minEntry.getFirst()) < 0) {
                minEntry = currentEntry;
                result = i;
            }
        }
        return result;
    }

    // there is a next element if  heads contains
    // at least of element not null.
    @Override
    public boolean hasNext() {
        return any(heads, x -> x != null);
    }

    @Override
    public Pair<T, List<V>> next() {
        // step one: determine nextIterator based on heads
        int i = nextIterator();
        if (i < 0) {
            throw new NoSuchElementException();
        }

        // step two: update values and determine result
        Pair<T, V> aux = heads.get(i);
        T nextStep = aux.getFirst();
        values.set(i, aux.getSecond());
        List<V> vs = new ArrayList<>(values);

        // step three: update heads
        Iterator<Pair<T, V>> t = iterators.get(i);
        heads.set(i, t.hasNext() ? t.next() : null);

        // now, everything is ready for the next call
        // of either hasNext or next
        return new Pair<>(nextStep, vs);
    }
}
