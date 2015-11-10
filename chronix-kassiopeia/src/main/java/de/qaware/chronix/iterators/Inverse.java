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

import static java.lang.Math.abs;

/**
 * @author johannes.siedersleben
 */
class Inverse implements InfiniteIterator<Double> {

    private static final double EPS = 1e-10;
    private final Iterator<Double> s;                 // the iterator to be inverted
    private final List<Double> xs = new ArrayList<>();    // read part of s
    private final List<Double> xt = new ArrayList<>();    // coefficients of 1/s

    /**
     * @param s the iterator to be elementwise inverted
     */
    public Inverse(Iterator<Double> s) {
        this.s = s;
        if (!s.hasNext()) {        // empty iterator cannot be inverted
            throw new IllegalArgumentException();
        }
        double x = s.next();
        if (abs(x) < EPS) {        // first coefficient must not be 0.0
            throw new IllegalArgumentException();
        }
        xs.add(x);                // first element of s read
        xt.add(1 / x);            // first element of inverse computed
    }

    /**
     * @return the next element
     */
    public Double next() {
        double result = xt.get(xt.size() - 1);  // result to be returned

        // get next element of s
        if (s.hasNext()) {
            xs.add(s.next());
        } else {
            xs.add(0.0);
        }

        // compute and save next element of inverse for next call of next
        double x = 0;
        int k = xt.size();
        for (int i = 0; i < k; i++) {
            x += xt.get(i) * xs.get(k - i);
        }
        xt.add(-x * xt.get(0));

        return result;
    }
}
