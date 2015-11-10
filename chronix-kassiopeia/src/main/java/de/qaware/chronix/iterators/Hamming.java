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
import java.util.List;

import static java.util.Arrays.asList;

/**
 * This a famous exercise attributed to R. W. Hamming and reported by Dijkstra
 * in "a discipline of programming", p. 129.
 * The problem is this: Let p1, p2, .. pn be n integers (normally but not necessarily primes).
 * Create an iterator yielding 1 and all multiples of p1, p2, .. pn in ascending order.
 * So, (2,3,5) gives 1, 2, 3, 4, 5, 6, 8, 9, 10, 12, 15, ...
 * <p/>
 * This solution is based on a formula used in next(): Let
 * ps = (p1, p2, .. , pn)
 * q be the list of all integers produced so far,
 * qmax = max(q) = last element of q
 * then the the next number is given by
 * m = min { p * x | p in ps, x in q, p*x > qmax }
 *
 * @author johannes.siedersleben
 */
class Hamming implements InfiniteIterator<Integer> {
    private static final int BIG = Integer.MAX_VALUE;

    private final List<Integer> ps;                       // list of primes
    private final List<Integer> q = new ArrayList<>();   // list of numbers produced

    /**
     * @param qs one or more positive integers
     */
    public Hamming(Integer... qs) {
        ps = asList(qs);
        if (ps.size() == 0) {
            throw new IllegalArgumentException();
        }
        q.add(1);
    }

    @Override
    public Integer next() {
        int qmax = q.get(q.size() - 1);            // largest element of q

        // m = min { p * x | p in ps, x in q, p*x > qmax }
        int m = BIG;
        for (int p : ps) {
            for (int x : q) {
                if ((p * x > qmax) && (p * x < m)) {
                    m = p * x;
                }
            }
        }

        q.add(m);

        return qmax;
    }
}
