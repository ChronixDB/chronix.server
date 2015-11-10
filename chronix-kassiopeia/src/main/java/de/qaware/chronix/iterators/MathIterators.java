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

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import static de.qaware.chronix.dts.WeakLogic.weakComparator;
import static de.qaware.chronix.iterators.Iterators.*;
import static java.lang.StrictMath.abs;

/**
 * @author johannes.siedersleben
 */
public final class MathIterators {

    private static final int RELEVANT_POLYNOM_LENGTH = 50;

    /**
     * Private default constructor.
     */
    private MathIterators() {
    }

    /**
     * @param epsilon the maximum difference of doubles considered equal
     * @return a comparator which
     * compares nulls correctly (two nulls are equal)
     * considers null as -oo
     * compares non-null x and y by testing abs(x - y) < epsilon
     */
    public static Comparator<Double> weakDoubleComparator(double epsilon) {
        return weakComparator((x, y) -> {
            if (abs(x - y) < epsilon) {
                return 0;
            } else {
                return x < y ? -1 : 1;
            }
        });
    }


    /**
     * @param delta the increment
     * @return a unary operator incrementing doubles by delta
     */
    public static UnaryOperator<Double> increment(Double delta) {
        return (Double x) ->
                x == null ? delta : x + delta;
    }


    /**
     * @param factor the factor to be applied
     * @return a unary operator multiplying doubles by factor
     */
    public static UnaryOperator<Double> times(Double factor) {
        return (Double x) ->
                x == null ? factor : x * factor;
    }

    /**
     * @return a random infinite iterator.
     */
    public static InfiniteIterator<Double> random() {
        return java.lang.Math::random;
    }


    /**
     * @param delta the value added at each step
     * @param start the start value
     * @return the arithmetic series starting at start value
     * Example: start = 7, delta = 5 produces 7, 12, 17, 22, ..
     */
    public static InfiniteIterator<Double> arithmeticSeries(double start, double delta) {
        return unaryGenerator(start, increment(delta));
    }



    /**
     * @param factor the factor multiplied with at each step
     * @param start  the start value
     * @return the geometric series
     */

    public static InfiniteIterator<Double> geometricSeries(double start, double factor) {
        return unaryGenerator(start, times(factor));
    }

    /**
     * @return the iterator of the exp coefficients
     */
    public static ImmutableIterator<Double> exp() {
        return map(faculty(), (Long x) -> 1.0 / x);
    }

    /**
     * @return the iterator of the sine coefficients
     */
    public static ImmutableIterator<Double> sin() {
        return map(cycle(0.0, 1.0, 0.0, -1.0), exp(), (a, b) -> a * b);
    }

    /**
     * @return the iterator of the cosine coefficients
     */
    public static ImmutableIterator<Double> cos() {
        return map(cycle(1.0, 0.0, -1.0, 0.0), exp(), (a, b) -> a * b);
    }

    /**
     * @param s   an iterator
     * @param inc the increment
     * @return an iterator all of whose elements are incremented by inc
     */
    public static ImmutableIterator<Double> add(Iterator<Double> s, double inc) {
        return map(s, x -> x + inc);
    }

    /**
     * @param s   an iterator containing at least of element
     * @param inc the increment
     * @return an iterator whose first element is incremented by inc.
     */
    @SuppressWarnings("unchecked")
    public static Iterator<Double> inc(Iterator<Double> s, double inc) {
        return concat(of(s.next() + inc), s);
    }

    /**
     * @param s an iterator
     * @param t another iterator
     * @return the element wise sum of s and t
     */
    public static ImmutableIterator<Double> add(Iterator<Double> s, Iterator<Double> t) {
        return map(s, t, (x, y) -> x + y);
    }

    /**
     * @param s      an iterator
     * @param factor a factor
     * @return an iterator whose elements are multiplied by factor
     */
    public static ImmutableIterator<Double> multiply(Iterator<Double> s, double factor) {
        return map(s, x -> x * factor);
    }

    /**
     * @param s an iterator
     * @param t another iterator
     * @return the polynomial product of s and t
     * Either of s and t may or may not be infinite. The result is infinite if
     * either of s and t is.
     */
    public static ImmutableIterator<Double> multiply(Iterator<Double> s, Iterator<Double> t) {
        return new Product(s, t);
    }

    /**
     * @param t an iterator
     * @return the polynomial square of t
     */
    public static ImmutableIterator<Double> square(Iterator<Double> t) {
        List<ImmutableIterator<Double>> s = tee(t, 2);
        return multiply(s.get(0), s.get(1));
    }

    /**
     * @param t an iterator; first element must not be 0
     * @return the polynomial inverse of t. Thus
     * multiply(t, inverse(t)) is (1.0, 0.0, 0.0, ...)
     */
    public static InfiniteIterator<Double> inverse(Iterator<Double> t) {
        return new Inverse(t);
    }

    /**
     * @param s an iterator
     * @param t another iterator which must contain at least of non zero element
     * @return the polynomial quotient of s and t. Thus
     * multiply(s, divide(t, s)) is t
     */
    public static ImmutableIterator<Double> divide(Iterator<Double> s, Iterator<Double> t) {
        return new Product(s, inverse(t));
    }

    /**
     * @param t   an iterator yielding the coefficients in ascending order:
     *            a0, a1, a2, ..
     *            may be finite or infinite
     * @param dim the desired dimension. Mandatory iff t is infinite
     * @return a polynom defined on doubles.
     */
    public static UnaryOperator<Double> polynom(Iterator<Double> t, int dim) {
        return new UnaryOperator<Double>() {
            private List<Double> rt = take(t, dim);

            @Override
            public Double apply(Double x) {
                BinaryOperator<Double> horner = (a, b) -> a * x + b;
                return reduce(reverse(rt), horner, 0.0);
            }
        };
    }


    /**
     * @param t a double iterator
     * @return polynom(t) as UnaryOperator
     */
    public static UnaryOperator<Double> polynom(Iterator<Double> t) {
        return polynom(t, RELEVANT_POLYNOM_LENGTH);
    }


    /**
     * @param t   the first iterator
     * @param s   the second iterator
     * @param dim the dimension
     * @return the coefficients of polynom(t) o polynom(s), thus
     * polynom(compose(t, s)) = compose(polynom(t), polynom(s))
     */
    public static Iterator<Double> compose(Iterator<Double> t, Iterator<Double> s, int dim) {
        List<Double> rt = take(t, dim);
        List<Double> rs = take(s, dim);

        BiFunction<Iterator<Double>, Double, Iterator<Double>> horner =
                (Iterator<Double> a, Double b) -> inc(multiply(a, rs.iterator()), b);

        return reduce(reverse(rt), horner, of(0.0));
    }


    /**
     * @param t the first iterator
     * @param s the second iterator
     * @return the coefficients of polynom(t) o polynom(s), thus
     * polynom(compose(t, s)) = compose(polynom(t), polynom(s))
     * only the first 50 elements of s and t are read.
     */
    public static Iterator<Double> compose(Iterator<Double> t, Iterator<Double> s) {
        return compose(t, s, RELEVANT_POLYNOM_LENGTH);
    }

    /**
     * an iterator yielding the Fibonacci series
     *
     * @return the fibonacci series as InfiniteIterator
     */
    static InfiniteIterator<Integer> fibonacci() {
        return binaryGenerator(1, 1, (a, b) -> a + b);
    }

    /**
     * @return an iterator yielding the faculty
     */
    static InfiniteIterator<Long> faculty() {
        return new InfiniteIterator<Long>() {
            private int counter = 0;
            private long nextValue = 1;

            @Override
            public Long next() {
                if (nextValue < 0) {
                    throw new NoSuchElementException();
                }

                long result = nextValue;
                nextValue *= ++counter;
                return result;
            }
        };
    }

    /**
     * This a famous exercise attributed to R. W. Hamming and reported by Dijkstra
     * in "a discipline of programming", p. 129.
     * The problem is this: Let p1, p2, .. pn be n integers (often but not necessarily primes).
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
     * @return a hamming series as InfiniteIterator
     */

    static InfiniteIterator<Integer> hamming(Integer... primes) {
        return new Hamming(primes);
    }
}
