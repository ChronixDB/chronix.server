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


import org.junit.Test;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import static de.qaware.chronix.iterators.Iterators.*;
import static de.qaware.chronix.iterators.MathIterators.*;
import static java.lang.StrictMath.abs;
import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertSame;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;


/**
 * Basic MathIterators unit tests.
 */
public class MathIteratorsTest {

    private final static int N = 100;
    private final static double delta = 1E-10;

    @Test
    public void testRandom() {
        Iterator<Double> rs = limit(random(), N);
        BinaryOperator<Double> acc = (a, b) -> a + b;
        double sum = reduce(rs, acc);
        assertTrue(sum > 0);
    }

    @Test
    public void testOthers() {
        UnaryOperator<Double> inc7 = increment(7.0);
        double d = 8.0;
        assertEquals(15.0, inc7.apply(d).doubleValue(), 0);

        UnaryOperator<Double> t7 = times(7.0);
        assertEquals(56.0, t7.apply(d).doubleValue(), 0);

        Iterator<Double> a = arithmeticSeries(0.0, 2.0);
        BinaryOperator<Double> acc = (x, y) -> x + y;
        double sum = reduce(limit(a, 10), acc);
        assertEquals(90.0, sum, 0);

        Iterator<Double> g = geometricSeries(1.0, 2.0);
        sum = reduce(limit(g, 10), acc);
        assertEquals(1023.0, sum, 0);
    }

    @Test
    public void testWeakDoubleComparator() {
        Double epsilon = 1e-8;
        Comparator<Double> cmp = weakDoubleComparator(epsilon);
        Double a = 3.0;
        Double b = 3.0 + epsilon * 10;
        Double c = 3.0 + epsilon;
        assertTrue(cmp.compare(null, null) == 0);
        assertTrue(cmp.compare(null, a) == -1);
        assertTrue(cmp.compare(a, null) == 1);
        assertSame(cmp.compare(a, b), -1);
        assertSame(cmp.compare(a, c), 0);
    }


    @Test
    public void testFibonacci() {
        Iterator<Integer> f10 = limit(fibonacci(), 10);
        BinaryOperator<Integer> acc = (a, b) -> a + b;
        int sum = reduce(f10, acc);
        assertEquals(143, sum);
    }

    @Test
    public void testFaculty() {
        Iterator<Long> f7 = limit(faculty(), 7);
        BinaryOperator<Long> acc = (a, b) -> a + b;
        long sum = reduce(f7, acc);
        assertEquals(874, sum);
    }

    @Test
    public void testHamming() {
        Iterator<Integer> r;
        int sum;

        try {
            hamming();
        } catch (IllegalArgumentException ignored) {
        }

        Iterator<Integer> h = hamming(2);
        BinaryOperator<Integer> acc = (a, b) -> a + b;

        sum = reduce(limit(h, 10), acc);
        assertEquals(1023, sum);

        Iterator<Integer> k = hamming(2, 5);
        sum = reduce(limit(k, 10), acc);
        assertEquals(123, sum);

        h = hamming(2);
        k = hamming(2, 5);
        List<Iterator<Integer>> m = asList(h, k);
        r = merge(m, true);
        sum = reduce(limit(r, 10), acc);
        assertEquals(45, sum);

        h = hamming(2);
        k = hamming(2, 5);
        r = merge(asList(h, k), true);
        sum = reduce(limit(r, 10), acc);
        assertEquals(45, sum);
    }


    @Test
    public void testMultiply() {

        Iterator<Double> s = repeat(1.0);
        Iterator<Double> t = repeat(1.0);
        Iterator<Double> r = multiply(s, t);
        BinaryOperator<Double> acc = (a, b) -> a + b;

        double sum = reduce(limit(r, N), acc);
        assertEquals(N * (N + 1) / 2, sum, delta);

        List<ImmutableIterator<Double>> ts = tee(repeat(1.0), 2);
        r = multiply(ts.get(0), ts.get(1));

        sum = reduce(limit(r, N), acc);
        assertEquals(N * (N + 1) / 2, sum, delta);
    }

    @Test
    public void testSquare() {
        Iterator<Double> s = repeat(1.0);
        Iterator<Double> r = square(s);
        BinaryOperator<Double> acc = (a, b) -> a + b;

        double sum = reduce(limit(r, N), acc);
        assertEquals(N * (N + 1) / 2, sum, delta);

        r = square(cos());
        s = square(sin());
        Iterator<Double> result = map(r, s, acc);

        Double one = result.next();
        assertEquals(1.0, one, delta);
        sum = reduce(limit(result, 10), acc);
        assertEquals(0.0, sum, delta);
    }

    @Test
    public void testInverse() {
        Iterator<Double> s = repeat(2.0);
        Iterator<Double> t = inverse(s);
        Iterator<Double> r = multiply(s, t);

        Double one = r.next();
        assertEquals(1.0, one, delta);
        BinaryOperator<Double> acc = (a, b) -> a + b;

        double sum = reduce(limit(r, N), acc);
        assertEquals(0.0, sum, delta);

        s = repeat(2.0);
        t = of(1.0);
        Iterator<Double> result = divide(s, t);
        assertTrue(weakEquals(repeat(2.0), result, 10));
    }

    @Test
    public void testPolynoms() {
        Iterator<Double> s = asList(1.0, 1.0).iterator();
        Iterator<Double> t = asList(1.0, 1.0).iterator();
        Iterator<Double> r = multiply(s, t);

        // (a + b)^2 = a^2 + 2ab + b^2
        assertTrue(weakEquals(r, asList(1.0, 2.0, 1.0).iterator(), 3));

        s = asList(1.0).iterator();
        t = asList(2.0, 3.0, 4.0).iterator();
        r = multiply(s, t);
        assertTrue(weakEquals(r, asList(2.0, 3.0, 4.0).iterator(), 3));

        s = asList(0.0).iterator();
        t = asList(2.0, 3.0, 4.0).iterator();
        r = multiply(s, t);
        assertTrue(weakEquals(r, asList(0.0, 0.0, 0.0).iterator(), 3));

        s = repeat(1.0);
        t = asList(1.0, 1.0).iterator();
        r = multiply(s, t);
        Iterator<Double> u = concat(of(1.0), repeat(2.0));
        assertTrue(weakEquals(r, u, 100));

        s = asList(1.0, 1.0, 1.0).iterator();
        r = square(s);
        assertTrue(weakEquals(r, asList(1.0, 2.0, 3.0, 2.0, 1.0).iterator(), 5));

        r = add(square(sin()), square(cos()));
        r = limit(r, 10);
        assertEquals(1.0, r.next().doubleValue(),0);
        assertEquals(0.0, reduce(r, (a, b) -> abs(a) + abs(b), 0.0), 10e-15);

        Iterator<Double> xs = concat(of(1.0), of(2.0), of(3.0));
        UnaryOperator<Double> p = polynom(xs);
        UnaryOperator<Double> q;

        assertEquals(1.0, p.apply(0.0).doubleValue(), 0);
        assertEquals(6.0, p.apply(1.0).doubleValue(), 0);
        assertEquals(2.0, p.apply(-1.0).doubleValue(), 0);
        assertEquals(17.0, p.apply(2.0).doubleValue(), 0);

        UnaryOperator<Double> v = polynom(sin(), 20);    // value 20 minimizes error
        Iterator<Double> ys = map(of(0.0, 1.0, 2.0, 3.0), (Double x) -> abs(v.apply(x) - Math.sin(x)));
        BiFunction<Double, Double, Double> acc = (x, y) -> x + y;

        assertEquals(0.0, reduce(ys, acc, 0.0), 2.5e-10);

        p = polynom(sin(), 20);
        q = polynom(square(sin()), 20);
        Double b = p.apply(2.0);
        Double c = q.apply(2.0);
        Double a = abs(b * b - c);

        assertEquals(b * b, c, 2.5e-7);
    }


    @Test
    public void testDivide() {
        List<Double> xs = asList(1.0, 0.0, -1.0);   // p(x) = 1 - x2
        List<Double> ys = asList(1.0, -1.0);
        Iterator<Double> u = multiply(ys.iterator(), divide(xs.iterator(), ys.iterator()));
        assertTrue(weakEquals(xs.iterator(), u, 3));

        u = multiply(xs.iterator(), divide(ys.iterator(), xs.iterator()));
        assertTrue(weakEquals(ys.iterator(), u, 2));

        xs = asList(1.0, 1.0);   // p(x) = 1 + x
        u = multiply(xs.iterator(), inverse(xs.iterator()));

        BinaryOperator<Double> acc = (x, y) -> x + y;
        assertEquals(1.0, reduce(limit(u, 5), acc).doubleValue(), 0);
    }


    @Test
    public void testCompose() {
        Iterator<Double> t = of(1.0, 2.0, 3.0);
        Iterator<Double> s = of(5.0, 7.0);
        Iterator<Double> r = compose(t, s, 5);
        BinaryOperator<Double> acc = (a, b) -> a + b;

        double sum = reduce(r, acc);
        assertEquals(457.0, sum, 0);

        t = of(1.0, 2.0, 3.0);
        s = of(0.0);
        r = compose(t, s, 5);
        sum = reduce(r, acc);
        assertEquals(1.0, sum, 0);

        t = of(1.0, 2.0, 3.0);
        s = of(1.0);
        r = compose(t, s, 5);
        sum = reduce(r, acc);
        assertEquals(6.0, sum, 0);

        t = of(1.0, 1.0, 1.0);
        s = of(1.0, 1.0);
        r = compose(t, s, 5);
        sum = reduce(r, acc);
        assertEquals(7.0, sum, 0);

        t = of(1.0, 0.0);
        s = of(0.0, 1.0);
        r = compose(t, s);
        sum = reduce(r, acc);
        assertEquals(1.0, sum, 0);

        t = of(0.0, 1.0, 2.0);
        s = of(0.0);
        r = compose(t, s);
        sum = reduce(r, acc);
        assertEquals(0.0, sum, 0);

        s = of(0.0, 1.0, 2.0);
        t = of(0.0);
        r = compose(t, s);
        sum = reduce(r, acc);
        assertEquals(0.0, sum, 0);

        s = of(0.0, 1.0, 2.0);
        t = of(0.0, 1.0);
        r = compose(t, s);
        sum = reduce(r, acc);
        assertEquals(3.0, sum, 0);
    }


    @Test
    public void testScalarAdd() {
        Iterator<Double> t = unaryGenerator(0.0, x -> x + 1.0);
        BinaryOperator<Double> acc = (a, b) -> a + b;

        double sum = reduce(limit(add(t, 2.0), 10), acc);
        assertEquals(65.0, sum, 0);
    }

    @Test
    public void testScalarMultiply() {
        Iterator<Double> t = unaryGenerator(0.0, x -> x + 1.0);
        BinaryOperator<Double> acc = (a, b) -> a + b;

        double sum = reduce(limit(multiply(t, 2.0), 10), acc);
        assertEquals(90.0, sum, 0);
    }
}