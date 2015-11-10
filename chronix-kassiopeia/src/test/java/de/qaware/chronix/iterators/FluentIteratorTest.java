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
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static com.google.common.collect.Iterators.concat;
import static de.qaware.chronix.dts.Pair.pairOf;
import static de.qaware.chronix.dts.WeakLogic.weakBinaryOperator;
import static de.qaware.chronix.iterators.FluentIterator.fluent;
import static de.qaware.chronix.iterators.Iterators.*;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


/**
 * Basic FluentIterator unit tests.
 */
public class FluentIteratorTest {

    @Test
    public void testConcat() {
        Iterator<Integer> r = fluent(unaryGenerator(1, x -> x + 1)).limit(5);
        Iterator<Integer> s = fluent(unaryGenerator(6, x -> x + 1)).limit(5);
        Iterator<Integer> t = fluent(unaryGenerator(11, x -> x + 1)).limit(5);

        t = concat(s, r, t);

        BinaryOperator<Integer> f = (a, b) -> a + b;
        int sum = fluent(t).reduce(f);
        assertEquals(120, sum);
    }




    @Test
    public void testKeep() {
        Iterator<Integer> t = of(null, null);
        List<Integer> xs = takeAll(keepLast(t));
        assertEquals(xs.get(0), null);
        assertEquals(xs.size(), 1);

        t = of(null, null);
        xs = takeAll(keepFirst(t));
        assertEquals(xs.get(0), null);
        assertEquals(xs.size(), 1);

        t = empty();
        xs = takeAll(keepFirst(t));
        assertEquals(xs.size(), 0);

        xs = takeAll(keepLast(t));
        assertEquals(xs.size(), 0);

        FluentIterator<Pair<Integer, Integer>> pt =
                fluent(of(pairOf(1, 10),
                        pairOf(1, 20),
                        pairOf(2, 20),
                        pairOf(2, 30)));

        List<Pair<Integer, Integer>> first = pt.keepFirst(Pair::getFirst).takeAll();
        assertEquals(first.get(0), pairOf(1, 10));
        assertEquals(first.get(1), pairOf(2, 20));

        pt = fluent(of(pairOf(1, 10),
                pairOf(1, 20),
                pairOf(2, 20),
                pairOf(2, 30)));

        List<Pair<Integer, Integer>> last = pt.keepLast(Pair::getFirst).takeAll();
        assertEquals(last.get(0), pairOf(1, 20));
        assertEquals(last.get(1), pairOf(2, 30));

        pt = fluent(of(pairOf(1, 10),
                pairOf(1, 20),
                pairOf(2, 20),
                pairOf(2, 30)));

        first = pt.keepFirst(Pair::getSecond).takeAll();
        assertEquals(first.get(0), pairOf(1, 10));
        assertEquals(first.get(1), pairOf(1, 20));
        assertEquals(first.get(2), pairOf(2, 30));

        pt = fluent(of(pairOf(1, 10),
                pairOf(1, 20),
                pairOf(2, 20),
                pairOf(2, 30)));

        last = pt.keepLast(Pair::getSecond).takeAll();
        assertEquals(last.get(0), pairOf(1, 10));
        assertEquals(last.get(1), pairOf(2, 20));
        assertEquals(last.get(2), pairOf(2, 30));
    }



    @Test
    public void testOf() {
        FluentIterator<Integer> xs = fluent(of(1, 2, 3, 4, 5));
        BinaryOperator<Integer> f = (a, b) -> a + b;
        int sum = xs.reduce(f);
        assertEquals(15, sum);
    }

    @Test
    public void testRepeat() {
        FluentIterator<Integer> as = fluent(repeat(100)).limit(11);
        BiFunction<Integer, Integer, Integer> f = (a, b) -> a + b;

        int sum = as.reduce(f, 0);
        assertEquals(1100, sum);

        FluentIterator<Integer> t = fluent(repeat(100, 0));
        if (t.hasNext()) {
            fail();
        }

        t = fluent(empty());
        if (t.hasNext()) {
            fail();
        }

        t = fluent(repeat(100, 3));
        sum = reduce(t, f, 0);
        assertEquals(300, sum);

        t = fluent(of(1000));
        BinaryOperator<Integer> op = (a, b) -> a + b;
        sum = reduce(t, weakBinaryOperator(op));
        assertEquals(1000, sum);
    }


    @Test
    public void testPeek() {
        List<Integer> xs = new ArrayList<>();
        FluentIterator<Integer> t = fluent(unaryGenerator(1, x -> x + 1));
        t.peek(xs::add).take(10);

        BinaryOperator<Integer> acc = (a, b) -> a + b;
        int sum = reduce(xs.iterator(), acc);
        assertEquals(55, sum);
    }

    @Test
    public void testReduce() {
        FluentIterator<Integer> t = fluent(repeat(10, 5));
        BinaryOperator<Integer> op = (a, b) -> a + b;
        int sum = t.reduce(op);
        assertEquals(50, sum);

        t = fluent(repeat(10, 5));
        BinaryOperator<Integer> mul = (a, b) -> a * b;
        int prod = t.reduce(mul);
        assertEquals(100000, prod);

        t = fluent(empty());
        prod = t.reduce(mul, 1);
        assertEquals(1, prod);

        t = fluent(of(1, 2, 3));
        FluentIterator<Integer> s = fluent(of(10, 20, 30, 40, 50));
        BiFunction<FluentIterator<Integer>, Integer, FluentIterator<Integer>> f = (z, x) -> z.map(a -> a * x);

        int result = t.reduce(f, s).reduce(op);
        assertEquals(900, result);
    }


    @Test
    public void testTee() {
        // apply tee to infinite iterator
        FluentIterator<Integer> t = fluent(unaryGenerator(0, x -> x + 1));
        List<FluentIterator<Integer>> forks = t.tee(5);
        BinaryOperator<Integer> acc = (a, b) -> a + b;

        for (FluentIterator<Integer> f : forks) {
            int sum = f.limit(10).reduce(acc);
            assertEquals(45, sum);
        }

        // apply tee to finite iterator
        t = fluent(unaryGenerator(0, x -> x + 1));
        forks = t.limit(10).tee(5);
        for (FluentIterator<Integer> f : forks) {
            int sum = f.reduce(acc);
            assertEquals(45, sum);
        }
    }


    @Test
    public void testZip() {
        FluentIterator<Integer> s = fluent(repeat(10));
        Iterator<Integer> t = repeat(20);
        FluentIterator<Pair<Integer, Integer>> st = s.zip(t, true);

        Function<Pair<Integer, Integer>, Integer> psum =
                q -> weakBinaryOperator((Integer x, Integer y) -> x + y).apply(q.getFirst(), q.getSecond());

        BinaryOperator<Integer> acc = (a, b) -> a + b;

        int sum = st.map(psum).limit(5).reduce(acc);
        assertEquals(150, sum);

        s = fluent(empty());
        sum = s.zip(repeat(20), true)
                .map(psum).limit(5)
                .reduce(acc);
        assertEquals(100, sum);

        FluentIterator<Integer> vs = fluent(of(10, 20, 30, 40, 50));
        Iterator<Boolean> bs = cycle(true, false);
        sum = vs.zip(bs, false)
                .map(Pair::getFirst)
                .limit(5)
                .reduce(acc);
        assertEquals(150, sum);
    }


    @Test
    public void testSkipDuplicates() {
        FluentIterator<Integer> s = fluent(empty());
        BiFunction<Integer, Integer, Integer> f = (a, b) -> a + b;
        int sum = s.skipDuplicates().reduce(f, 0);
        assertEquals(0, sum);

        s = fluent(repeat(17, 100));
        BinaryOperator<Integer> acc = (a, b) -> a + b;
        sum = s.skipDuplicates().reduce(acc);
        assertEquals(17, sum);

        FluentIterator<Integer> r = fluent(repeat(17, 100)).concat(repeat(18, 100));
        sum = r.skipDuplicates().reduce(acc);
        assertEquals(35, sum);

        r = fluent(repeat(7, 100)).concat(repeat(10, 100));       // 7%3 = 10%3 = 1
        List<Integer> xs = r.skipDuplicates(x -> x % 3).takeAll();
        assertEquals(1, xs.size());
        assertEquals((Integer) 7, xs.get(0));

        xs = fluent(of(2)).skipDuplicates().takeAll();
        assertEquals(1, xs.size());

        xs = fluent(of(2, 3)).skipDuplicates().takeAll();
        assertEquals(2, xs.size());
    }

    @Test
    public void testGroupBy() {
        FluentIterator<Integer> s = fluent(empty());
        FluentIterator<Integer> t = s.concat(repeat(17, 100), repeat(27, 100));

        BinaryOperator<Integer> f = (a, b) -> a + b;
        int sum = t.groupBy(x -> x % 6).map(List::size).reduce(f);
        assertEquals(200, sum);

        t = fluent(unaryGenerator(0, x -> x + 1));
        sum = t.groupBy()
                .map(List::size)
                .limit(10)
                .reduce(f);
        assertEquals(10, sum);

        t = fluent(empty());
        Iterator<List<Integer>> g = groupBy(t);
        assertFalse(g.hasNext());

        s = fluent(unaryGenerator(0, x -> x + 1)).limit(20);
        t = fluent(repeat(1, 0)).concat(repeat(2000, 10), repeat(3000, 10));
        List<List<Pair<Integer, Integer>>> ps = s.zip(t, true).groupBy(Pair::getSecond).takeAll();
        assertEquals(2, ps.size());
    }


    @Test
    public void testWeakEquals() {

        FluentIterator<Integer> ta = fluent(of(10, 20, 30));
        FluentIterator<Integer> tb = fluent(of(10, 20, 30));
        assertTrue(ta.weakEquals(tb, 2));

        ta = fluent(of(10, 20, 30));
        tb = fluent(of(10, 20, 30));
        assertTrue(ta.weakEquals(tb, 3));

        ta = fluent(of(10, 20, 30));
        tb = fluent(of(10, 20, 30));
        assertTrue(ta.weakEquals(tb, 4));

        tb = fluent(of(10, 20, 30));
        Iterator<Integer> tc = of(10, 20, 30, 40);
        assertTrue(tb.weakEquals(tc, 3));
        assertFalse(tb.weakEquals(tc, 4));

        Iterator<Integer> tg = of(10, null, null);

        FluentIterator<Integer> te = fluent(empty());
        Iterator<Integer> tf = empty();
        assertTrue(te.weakEquals(tf, 3));

        te = fluent(empty());
        assertTrue(te.weakEquals(te, 4));

        FluentIterator<Integer> td = fluent(of(10, null, null));
        assertTrue(weakEquals(td, tg, 3));

        td = fluent(of(10, 20, 30, 40));
        tf = of(10, null, null);
        assertFalse(td.weakEquals(tf, 4));
    }


    @Test
    public void testTake() {
        List<Integer> xs = fluent(repeat(27, 100)).take(0);
        assertEquals(0, xs.size());

        xs = fluent(repeat(27, 100)).take(39);
        assertEquals(39, xs.size());

        xs = fluent(repeat(27, 100)).take(101);
        assertEquals(100, xs.size());
    }

    @Test
    public void testMap() {
        BinaryOperator<Integer> op = (a, b) -> a + b;
        BinaryOperator<Integer> wop = weakBinaryOperator(op);

        int sum = fluent(repeat(3, 10))
                .map(repeat(4, 11), wop)
                .reduce(op);
        assertEquals(74, sum);
    }

    @Test
    public void testFilter() {
        FluentIterator<Integer> t = fluent(unaryGenerator(1, x -> x + 1)).limit(10);
        BinaryOperator<Integer> acc = (a, b) -> a + b;

        int sum = reduce(t.filter(x -> x % 2 == 0), acc);
        assertEquals(30, sum);
    }

    @Test
    public void testAllAny() {
        FluentIterator<Integer> t = fluent(unaryGenerator(1, x -> x + 1)).limit(10);
        assertFalse(t.all(x -> x % 2 == 0));

        t = fluent(unaryGenerator(1, x -> x + 1)).limit(10);
        assertTrue(t.any(x -> x % 2 == 0));

        t = fluent(unaryGenerator(0, x -> x + 2)).limit(100);
        assertTrue(t.all(x -> x % 2 == 0));

        t = fluent(unaryGenerator(0, x -> x + 2)).limit(100);
        assertTrue(t.any(x -> x % 2 == 0));

        t = fluent(unaryGenerator(0, x -> x + 2)).limit(100);
        assertFalse(t.all(x -> x % 2 == 1));

        t = fluent(unaryGenerator(0, x -> x + 2)).limit(100);
        assertFalse(t.any(x -> x % 2 == 1));
    }


    @Test
    public void testReadme() {
        FluentIterator<Integer> s;
        FluentIterator<Integer> t;
        List<Integer> xs;

        // all, any
        t = fluent(unaryGenerator(0, x -> x + 1)).limit(10);
        assertFalse(t.all(x -> x % 2 == 0));                    // returns false
        t = fluent(unaryGenerator(0, x -> x + 1)).limit(10);
        t.any(x -> x % 2 == 0);                    // returns true

        // filter, peek, take
        t = fluent(unaryGenerator(0, x -> x + 1));
        StringBuilder buf = new StringBuilder();
        s = t.filter(x -> x % 2 == 0);              // returns 0, 2, 4, ...
        xs = s.peek(buf::append).take(5);           // xs = [0, 2, 4, 6, 8], buf = "02468"
        assertEquals(xs.get(4), Integer.valueOf(8));
        assertEquals(buf.toString(), "02468");

        // map, reduce
        t = fluent(unaryGenerator(0, x -> x + 1)).limit(5);
        assertEquals(t.map(x -> 2 * x).reduce((x, y) -> x + y), Integer.valueOf(20));

        // tee, zip
        t = fluent(unaryGenerator(0, x -> x + 1));
        List<FluentIterator<Integer>> forks = t.tee(2);    // two copies of t
        FluentIterator<Pair<Integer, Integer>> r = forks.get(0).zip(forks.get(1), true);
        xs = r.map(p -> p.getFirst() + p.getSecond()).take(5);
        assertEquals(xs.get(4), Integer.valueOf(8));
    }
}