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

/**
 * @author johannes.siedersleben
 */
class Product implements ImmutableIterator<Double> {

    private final Iterator<Double> s;                // the factors
    private final Iterator<Double> t;
    private final List<Double> hs = new ArrayList<>();    // head of s
    private final List<Double> ht = new ArrayList<>();    // head of t
    private int current;                 // index of next element of product

    /**
     * @param s the first factor
     * @param t the second factor
     */
    public Product(Iterator<Double> s, Iterator<Double> t) {
        if (!s.hasNext()) {
            throw new IllegalArgumentException("first factor is empty");
        }

        if (!t.hasNext()) {
            throw new IllegalArgumentException("second factor is empty");
        }

        this.s = s;
        this.t = t;
        current = 0;
        if (s.hasNext()) {
            hs.add(s.next());
        }
        if (t.hasNext()) {
            ht.add(t.next());
        }
    }

    /**
     * @return true iff there is a next element
     */
    public boolean hasNext() {
        return current < hs.size() + ht.size() - 1;
    }

    /**
     * @return the next element
     */
    public Double next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        double result = 0.0;
        for (int i = 0; i <= current; i++) {
            double xs = (i < hs.size()) ? hs.get(i) : 0.0;
            double xt = (current - i < ht.size()) ? ht.get(current - i) : 0.0;
            result += xs * xt;
        }

        current++;
        if (s.hasNext()) {
            hs.add(s.next());
        }
        if (t.hasNext()) {
            ht.add(t.next());
        }
        return result;
    }
}
