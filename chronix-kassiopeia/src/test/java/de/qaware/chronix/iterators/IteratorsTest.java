/*
 * #%L
 * QAcommons - The QAware Standard Library - Iterators
 * %%
 * Copyright (C) 2014 QAware GmbH
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package de.qaware.chronix.iterators;


import de.qaware.chronix.dts.Pair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

import static de.qaware.chronix.dts.Functions.compose;
import static de.qaware.chronix.dts.Pair.pairOf;
import static de.qaware.chronix.dts.WeakLogic.weakBinaryOperator;
import static de.qaware.chronix.iterators.Iterators.*;
import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Basic Iterators tests.
 */
public class IteratorsTest {

    @Test
    public void testImmutableIterator() {
        ImmutableIterator<String> t = of("xxx");
        try {
            t.remove();
        } catch (UnsupportedOperationException e) {
            //ignored
        }
    }


    @Test
    public void testGenerate() {
        ImmutableIterator<String> t = generate(() -> "ab");
        assertEquals(t.next(), "ab");
        assertEquals(t.next(), "ab");
        assertEquals(t.next(), "ab");
    }

    @Test
    public void testCompose() {
        Function<Pair<String, Integer>, Integer> f = Pair::getSecond;
        Function<Integer, Integer> g = x -> 2 * x;
        Function<Pair<String, Integer>, Integer> h = compose(g, f);
        Pair<String, Integer> p = new Pair<>("hello", 7);
        int s = h.apply(p);
        assertEquals(14, s);
    }

    @Test
    public void testUnaryGenerator() {
        Iterator<Integer> t = limit(unaryGenerator(1, x -> x * 3), 10);
        BinaryOperator<Integer> acc = (a, b) -> a + b;
        int sum = reduce(t, acc);
        assertEquals(29524, sum);
    }

    @Test
    public void testOf() {
        Iterator<Integer> xs = of(1, 2, 3, 4, 5);
        BinaryOperator<Integer> acc = (a, b) -> a + b;
        int sum = reduce(xs, acc);
        assertEquals(15, sum);
    }

    @Test
    public void testFilter() {
        List<Integer> xs = take(unaryGenerator(1, x -> x + 1), 10);
        Predicate<Integer> p = x -> x % 2 == 0;
        BinaryOperator<Integer> add = (a, b) -> a + b;
        int sum = reduce(filter(xs, p), add);
        assertEquals(30, sum);
    }

    @Test
    public void testConcat() {
        Iterator<Integer> xs = of(1, 2, 3, 4, 5);
        Iterator<Integer> ys = of(11, 12, 13, 14, 15);
        BinaryOperator<Integer> acc = (a, b) -> a + b;

        int sum = reduce(concat(xs, ys), acc);
        assertEquals(80, sum);

        xs = of(1, 2, 3, 4, 5);
        ys = of();
        sum = reduce(concat(xs, ys), acc);
        assertEquals(15, sum);

        xs = of();
        ys = of(11, 12, 13, 14, 15);
        sum = reduce(concat(xs, ys), acc);
        assertEquals(65, sum);

        xs = of(1, 2, 3, 4, 5);
        sum = reduce(concat(xs), acc);
        assertEquals(15, sum);

        sum = reduce(concat(), (Integer a, Integer b) -> a + b, 0);
        assertEquals(0, sum);

        xs = of(1, 2, 3, 4, 5);
        ys = of(11, 12, 13, 14, 15);
        Iterator<Integer> zs = of(21, 22, 23, 24, 25);
        sum = reduce(concat(xs, ys, zs), acc);
        assertEquals(195, sum);

        sum = reduce(concat(xs, ys, zs), acc, 0);
        assertEquals(0, sum);
    }

    @Test
    public void testCycle() {
        Iterator<Integer> xs = limit(cycle(2), 10);
        BinaryOperator<Integer> acc = (a, b) -> a + b;

        int sum = reduce(xs, acc);
        assertEquals(20, sum);

        xs = limit(cycle(1, 2), 10);

        sum = reduce(xs, acc);
        assertEquals(15, sum);
    }


    @Test
    public void testRepeat() {

        Iterator<Integer> as = limit(repeat(100), 11);
        BiFunction<Integer, Integer, Integer> acc = (a, b) -> a + b;
        int sum = reduce(as, acc, 0);
        assertEquals(1100, sum);

        Iterator<Integer> t = repeat(100, 0);
        if (t.hasNext()) {
            fail();
        }

        t = empty();
        if (t.hasNext()) {
            fail();
        }

        t = repeat(100, 3);
        sum = reduce(t, acc, 0);
        assertEquals(300, sum);

        t = of(1000);
        BinaryOperator<Integer> op = (a, b) -> a + b;
        sum = reduce(t, weakBinaryOperator(op));
        assertEquals(1000, sum);
    }

    @Test
    public void testPeek() {
        List<Integer> xs = new ArrayList<>();
        Iterator<Integer> t = unaryGenerator(1, x -> x + 1);
        take(peek(t, xs::add), 10);
        BinaryOperator<Integer> acc = (a, b) -> a + b;
        int sum = reduce(xs.iterator(), acc);
        assertEquals(55, sum);
    }

    @Test
    public void testReduce() {
        Iterator<Integer> t = repeat(10, 5);
        BinaryOperator<Integer> acc = (a, b) -> a + b;
        int sum = reduce(t, acc);
        assertEquals(sum, 50);

        t = repeat(10, 5);
        BinaryOperator<Integer> mul = (a, b) -> a * b;
        int prod = reduce(t, mul);
        assertEquals(prod, 100000);

        t = empty();
        prod = reduce(t, mul, 1);
        assertEquals(prod, 1);

        t = of(1, 2, 3);
        Iterator<Integer> s = of(10, 20, 30, 40, 50);
        BiFunction<Iterator<Integer>, Integer, Iterator<Integer>> f = (z, x) -> map(z, a -> a * x);
        Iterator<Integer> result = reduce(t, f, s);
        sum = reduce(result, acc);
        assertEquals(sum, 900);
    }

    @Test
    public void testMerge() {
        List<Iterator<Integer>> ts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ts.add(repeat(100 * i, i));
        }
        Iterator<Integer> r = merge(ts, true);
        BiFunction<Integer, Integer, Integer> acc = (a, b) -> a + b;
        int sum = reduce(r, acc, 0);
        assertEquals(sum, 3000);

        ts.clear();
        ts.add(of());
        ts.add(of(1, 2, 3, 4));
        r = merge(ts, true);
        assertEquals(takeAll(r).size(), 4);

        ts.clear();
        ts.add(of());
        ts.add(of(1, 2, 3, 4));
        r = merge(ts, false);
        assertEquals(takeAll(r).size(), 0);

        ts.clear();
        ts.add(of(1, 2));
        ts.add(of(1, 2, 3, 4));
        r = merge(ts, false);
        List<Integer> xs = takeAll(r);

        // assertEquals(takeAll(r).size(), 4);
    }

    @Test
    public void testTee() {

        // apply tee to infinite iterator
        Iterator<Integer> t = unaryGenerator(0, x -> x + 1);
        List<ImmutableIterator<Integer>> fork = tee(t, 5);
        BinaryOperator<Integer> acc = (a, b) -> a + b;
        for (Iterator<Integer> f : fork) {
            int sum = reduce(limit(f, 10), acc);
            assertEquals(45, sum);
        }

        // apply tee to finite iterator
        Iterator<Integer> xs = limit(unaryGenerator(0, x -> x + 1), 10);
        fork = tee(xs, 5);
        for (Iterator<Integer> f : fork) {
            int sum = reduce(f, acc);
            assertEquals(45, sum);
        }
    }

    @Test
    public void testZipLists() {
        Iterator<Integer> s = repeat(10);
        Iterator<Integer> t = repeat(20);
        List<Iterator<Integer>> m = asList(s, t);

        Iterator<List<Integer>> rs = zip(m);

        Function<List<Integer>, Integer> wsum =
                xs -> {
                    BinaryOperator<Integer> op = (a, b) -> a + b;
                    return reduce(asIterator(xs), weakBinaryOperator(op));
                };

        Iterator<Integer> us = map(rs, wsum);
        BinaryOperator<Integer> acc = (a, b) -> a + b;
        int total = reduce(limit(us, 5), acc);
        assertEquals(150, total);

        s = empty();
        t = repeat(20);
        m = asList(s, t);

        rs = zip(m);

        us = map(rs, wsum);
        total = reduce(limit(us, 5), acc);
        assertEquals(100, total);
    }

    @Test
    public void testZipPairs() {
        Iterator<Integer> s = repeat(10);
        Iterator<Integer> t = repeat(20);
        Pair<Iterator<Integer>, Iterator<Integer>> p = pairOf(s, t);

        Iterator<Pair<Integer, Integer>> rs = zip(p, true);

        Function<Pair<Integer, Integer>, Integer> psum =
                q -> weakBinaryOperator((Integer x, Integer y) -> x + y).apply(q.getFirst(), q.getSecond());

        Iterator<Integer> us = map(rs, psum);
        BinaryOperator<Integer> acc = (a, b) -> a + b;

        int sum = reduce(limit(us, 5), acc);
        assertEquals(150, sum);

        s = empty();
        t = repeat(20);
        p = pairOf(s, t);

        rs = zip(p, true);                 // rs yields pairs of integer, the first element being null
        us = map(rs, psum);                // psum adds pairs of integers accepting nulls

        sum = reduce(limit(us, 5), acc);
        assertEquals(100, sum);

        Iterator<Integer> vs = of(10, 20, 30, 40, 50);
        Iterator<Boolean> bs = cycle(true, false);
        Iterator<Pair<Integer, Boolean>> pt = zip(pairOf(vs, bs), false);
        Iterator<Integer> qt = map(pt, Pair::getFirst);
        sum = reduce(limit(qt, 5), acc);
        assertEquals(150, sum);
    }

    @Test
    public void testUnzip() {
        Iterator<Integer> s = repeat(10);
        Iterator<Integer> t = repeat(20);
        List<Iterator<Integer>> m = asList(s, t);

        Iterator<List<Integer>> rs = zip(m);
        List<ImmutableIterator<Integer>> zs = unzip(rs);

        BinaryOperator<Integer> acc = (a, b) -> a + b;

        int sum0 = reduce(limit(zs.get(0), 5), acc);
        int sum1 = reduce(limit(zs.get(1), 5), acc);

        assertEquals(50, sum0);
        assertEquals(100, sum1);

        // cool = ((100, 200, 300), (101, 201, 301), (102, 202, 302), ...)
        Iterator<List<Integer>> cool =
                unaryGenerator(asList(100, 200, 300), (List<Integer> xs) ->
                        take(map(asIterator(xs), (Integer x) -> x + 1), 3));


        zs = unzip(take(cool, 5).iterator());

        sum0 = reduce(zs.get(0), acc);      // 100 + 101 + ... + 104  = 510
        sum1 = reduce(zs.get(1), acc);      // 200 + 201 + ... + 204  = 1010
        int sum2 = reduce(zs.get(2), acc);  // 300 + 301 + ... + 304  = 1510

        assertEquals(510, sum0);
        assertEquals(1010, sum1);
        assertEquals(1510, sum2);

        List<Integer> a = asList(1, 2, 3);
        List<Integer> b = asList(1, 2, 3, 4);
        List<Integer> c = asList(1, 2);
        List<Integer> d = asList(1);
        List<Integer> e = asList();

        Iterator<List<Integer>> ts = of(a, b, c, d, e);
        List<Integer> y;
        BinaryOperator<Integer> wadd = weakBinaryOperator((u, v) -> u + v);
        List<ImmutableIterator<Integer>> us = unzip(ts);

        assertEquals((Integer) 4, reduce(us.get(0), wadd));
        assertEquals((Integer) 6, reduce(us.get(1), wadd));
        assertEquals((Integer) 6, reduce(us.get(2), wadd));
    }

    @Test
    public void testKeepFirst() {
        Iterator<Integer> s = empty();
        BiFunction<Integer, Integer, Integer> acc = (a, b) -> a + b;
        int sum = reduce(keepFirst(s), acc, 0);
        assertEquals(0, sum);

        s = repeat(17, 100);
        BinaryOperator<Integer> op = (a, b) -> a + b;
        sum = reduce(keepFirst(s), op);
        assertEquals(17, sum);

        Iterator<Integer> r = concat(repeat(17, 100), repeat(18, 100));
        sum = reduce(keepFirst(r), op);
        assertEquals(35, sum);

        r = concat(repeat(7, 100), repeat(10, 100));       // 7%3 = 10%3 = 1
        List<Integer> xs = takeAll(keepFirst(r, x -> x % 3));
        assertEquals(1, xs.size());
        assertEquals((Integer) 7, xs.get(0));

        xs = takeAll(keepFirst(of(2)));
        assertEquals(1, xs.size());

        xs = takeAll(keepFirst(of(2, 3)));
        assertEquals(2, xs.size());
    }


    @Test
    public void testKeepLast() {
        Iterator<Integer> s = empty();
        BiFunction<Integer, Integer, Integer> acc = (a, b) -> a + b;
        int sum = reduce(keepLast(s), acc, 0);
        assertEquals(0, sum);

        s = repeat(17, 100);
        BinaryOperator<Integer> op = (a, b) -> a + b;
        sum = reduce(keepLast(s), op);
        assertEquals(17, sum);

        Iterator<Integer> r = concat(repeat(17, 100), repeat(18, 100));
        sum = reduce(keepLast(r), op);
        assertEquals(35, sum);

        r = concat(repeat(7, 100), repeat(10, 100));       // 7%3 = 10%3 = 1
        List<Integer> xs = takeAll(keepLast(r, x -> x % 3));
        assertEquals(1, xs.size());
        assertEquals((Integer) 10, xs.get(0));

        xs = takeAll(keepLast(of(2)));
        assertEquals(1, xs.size());

        xs = takeAll(keepLast(of(2, 3)));
        assertEquals(2, xs.size());
    }


    @Test
    public void testSkipDuplicates() {
        // skipDuplicates is equivalent to takeFirst

        Iterator<Integer> r = concat(repeat(17, 100), repeat(18, 100));
        BinaryOperator<Integer> acc = (a, b) -> a + b;

        int sum = reduce(skipDuplicates(r), acc);
        assertEquals(35, sum);

        r = concat(repeat(7, 100), repeat(10, 100));       // 7%3 = 10%3 = 1
        List<Integer> xs = takeAll(skipDuplicates(r, x -> x % 3));
        assertEquals(1, xs.size());
        assertEquals((Integer) 7, xs.get(0));
    }


    @Test
    public void testGroupBy() {
        Iterator<Integer> s;
        Iterator<Integer> t = concat(repeat(17, 100), repeat(27, 100));

        Iterator<Integer> sizes = map(groupBy(t, x -> x % 6), (List<Integer> xs) -> xs.size());
        BinaryOperator<Integer> acc = (a, b) -> a + b;

        int sum = reduce(sizes, acc);
        assertEquals(200, sum);

        t = unaryGenerator(0, x -> x + 1);
        sizes = map(take(groupBy(t), 10).iterator(), (List<Integer> xs) -> xs.size());
        sum = reduce(sizes, acc);
        assertEquals(10, sum);

        sizes = map(take(groupBy(t), 10).iterator(), (List<Integer> xs) -> xs.size());
        sum = reduce(sizes, acc);
        assertEquals(10, sum);

        t = empty();
        Iterator<List<Integer>> g = groupBy(t);
        assertFalse(g.hasNext());

        s = limit(unaryGenerator(0, x -> x + 1), 20);
        t = concat(repeat(2000, 10), repeat(3000, 10));
        Iterator<Pair<Integer, Integer>> r = zip(pairOf(s, t), true);

        Iterator<List<Pair<Integer, Integer>>> u = groupBy(r, Pair::getSecond);

        List<List<Pair<Integer, Integer>>> ps = takeAll(u);

        assertEquals(2, ps.size());

    }


    @Test
    public void testEqual() {
        Iterator<Integer> ta = of(10, 20, 30);
        Iterator<Integer> tb = of(10, 20, 30);
        Iterator<Integer> tc;
        Iterator<Integer> td;
        Iterator<Integer> tg = of(10, null, null);
        Iterator<Integer> te = empty();
        Iterator<Integer> tf = empty();

        assertTrue(weakEquals(ta, tb, 2));

        ta = of(10, 20, 30);
        tb = of(10, 20, 30);
        assertTrue(weakEquals(ta, tb, 3));

        ta = of(10, 20, 30);
        tb = of(10, 20, 30);
        assertTrue(weakEquals(ta, tb, 4));

        tb = of(10, 20, 30);
        tc = of(10, 20, 30, 40);
        assertTrue(weakEquals(tb, tc, 3));
        assertFalse(weakEquals(tb, tc, 4));

        assertTrue(weakEquals(te, tf, 3));

        te = empty();
        assertTrue(weakEquals(te, te, 4));

        td = of(10, null, null);
        assertTrue(weakEquals(td, tg, 3));

        tc = of(10, 20, 30, 40);
        td = of(10, null, null);
        assertFalse(weakEquals(tc, td, 4));
    }

    @Test
    public void testMerge1() {
        List<Iterator<Integer>> ts = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            ts.add(repeat(100 * i, i));
        }

        // m1 returns the index of the smallest element of a list or -1 if there are only nulls
        Function<List<Integer>, Integer> m1 = xs -> {
            Integer result = -1;
            for (int i = 0; i < xs.size(); i++) {
                if (xs.get(i) == null) {
                    continue;
                }
                if (result == -1 || xs.get(i) < xs.get(result)) {
                    result = i;
                }
            }
            return result;
        };

        Iterator<Integer> r = new Merge<>(ts, m1, true);
        BinaryOperator<Integer> add = (a, b) -> a + b;
        int sum = reduce(r, add, 0);
        assertEquals(3000, sum);
    }


    @Test
    public void testSpecials() {
        Iterator<Pair<Integer, Integer>> timestamps = of(
                pairOf(0, 10),
                pairOf(0, 20),
                pairOf(0, 30),
                pairOf(10, 40),
                pairOf(10, 50),
                pairOf(10, 30),
                pairOf(20, 60),
                pairOf(20, 70),
                pairOf(20, 30)
        );

        // keep last duplicate timestamp
        Iterator<Pair<Integer, Integer>> aux2 = keepLast(timestamps, Pair::getFirst);

        // keep first duplicate value, discarding identical values
        List<Pair<Integer, Integer>> points = takeAll(keepFirst(aux2, Pair::getSecond));

        assertEquals(1, points.size());
        assertEquals(pairOf(0, 30), points.get(0));
    }

    @Test
    public void testReadme() {
        ImmutableIterator<Integer> s;
        ImmutableIterator<Integer> t;
        List<Integer> xs;

        // all, any
        t = limit(unaryGenerator(0, x -> x + 1), 10);
        assertFalse(all(t, (x -> x % 2 == 0)));
        t = limit(unaryGenerator(0, x -> x + 1), 10);
        assertTrue(any(t, x -> x % 2 == 0));

        // filter, peek, take
        t = unaryGenerator(0, x -> x + 1);
        StringBuilder buf = new StringBuilder();
        s = filter(t, x -> x % 2 == 0);              // returns 0, 2, 4, ...
        xs = take(peek(s, buf::append), 5);          // xs = [0, 2, 4, 6, 8], buf = "02468"
        assertEquals(xs.get(4), Integer.valueOf(8));
        assertEquals(buf.toString(), "02468");

        // map, reduce
        t = limit(unaryGenerator(0, x -> x + 1), 5);
        BinaryOperator<Integer> acc = (x, y) -> x + y;
        assertEquals(reduce((map(t, x -> 2 * x)), acc), Integer.valueOf(20));

        // tee, zip
        t = unaryGenerator(0, x -> x + 1);
        List<ImmutableIterator<Integer>> forks = tee(t, 2);       // two copies of t

        // zip needs a List<Iterator<Integer>> rather than
        // a List<ImmutableIterator<Integer>
        ImmutableIterator<List<Integer>> r = zip(new ArrayList<>(forks));
        xs = take(map(r, p -> p.get(0) + p.get(1)), 5);
        assertEquals(xs.get(4), Integer.valueOf(8));

        // merge
        s = of(0, 2);
        t = of(1, 3, 5, 7);
        ImmutableIterator<Integer> v = merge(asList(t, s), true);        // [0, 1, 2, 3, 5, 7]
        assertEquals(reduce(v, acc), Integer.valueOf(18));

        s = of(0, 3);
        t = of(1, 2, 5, 7);
        ImmutableIterator<Integer> w = merge(asList(t, s), false);       // [0, 1, 2, 3]
        assertEquals(reduce(w, acc), Integer.valueOf(6));

        s = of(0, 2);
        t = of(1, 3, 5, 7);
        w = merge(asList(t, s), false);                                   // [0, 1, 2]
        assertEquals(reduce(w, acc), Integer.valueOf(3));


        // groupBy, skipDuplicates, keepFirst, keepLast
        s = of(1, 1, 2, 2, 2, 3);
        ImmutableIterator<List<Integer>> r1 = groupBy(s);  // [[1, 1], [2, 2, 2], [3]]
        assertEquals(takeAll(r1).size(), 3);

        s = of(1, 1, 2, 2, 2, 3);
        r1 = groupBy(s);  // [[1, 1], [2, 2, 2], [3]]
        assertEquals(reduce(map(r1, List::size), acc), Integer.valueOf(6));

        s = of(1, 3, 2, 4, 6, 8);
        ImmutableIterator<List<Integer>> r2 = groupBy(s, x -> x % 2);  // [[1, 3], [2, 4, 6, 8]]
        assertEquals(reduce(map(r2, List::size), acc), Integer.valueOf(6));
    }

}