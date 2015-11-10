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

import java.util.*;

/**
 * This class creates n iterators based on of, called base.
 * These iterators can be iterated on independently.
 * Base must not be touched after being used for Tee.
 * Main idea: There is of queue per iterator created;
 * each queue contains the elements not yet read by this iterator
 * but read from base by somebody else.
 * So, this happens on next ("I" am of of the n Iterators created):
 * If my queue is empty: I get the next element from base and union it to ALL OTHER queues.
 * If !base.hasNext(), then so am I.
 * If not, my queue is not empty. I remove its head and return it.
 * The iterators are created by means of the inner class Aux which implements
 * the iterator interface.
 * Class Tee is for internal use by Iterators only.
 *
 * @param <T> any type
 * @author johannes.siedersleben
 */
class Tee<T> {
    private final Iterator<T> base;
    private final int n;
    private final Queue<T>[] queues;

    /**
     * @param base the iterator to be forked
     * @param n    the number of tines
     */
    public Tee(Iterator<T> base, int n) {
        this.base = base;
        this.n = n;
        queues = new LinkedList[n];
        for (int i = 0; i < n; i++) {
            queues[i] = new LinkedList<>();
        }
    }

    /**
     * @return a fork of n tines, each tine being an independent copy of base
     */
    public List<ImmutableIterator<T>> iterators() {
        List<ImmutableIterator<T>> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            result.add(new Aux(queues[i]));
        }
        return result;
    }

    private void add(T x) {
        for (Queue<T> q : queues) {
            q.add(x);
        }
    }

    private class Aux implements ImmutableIterator<T> {
        private final Queue<T> q;

        Aux(Queue<T> q) {
            this.q = q;
        }

        @Override
        public boolean hasNext() {
            return q.size() > 0 || base.hasNext();
        }

        @Override
        public T next() {
            if (q.isEmpty()) {
                T x = base.next();  // throws NoSuchElementException.
                add(x);
            }
            return q.remove();       // q is not empty unless base is at end
        }
    }
}
