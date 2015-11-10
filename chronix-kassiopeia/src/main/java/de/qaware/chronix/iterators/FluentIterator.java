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


import de.qaware.chronix.dts.Pair;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.*;

import static de.qaware.chronix.dts.Pair.pairOf;

/**
 * An iterator implementation that allows chaining using a fluent API.
 *
 * @param <T> any type
 * @author johannes.siedersleben
 */
public class FluentIterator<T> implements ImmutableIterator<T> {

    private final Iterator<T> iterator;

    /**
     * @param t an iterator to be transformed into a fluent one
     */
    public FluentIterator(Iterator<T> t) {
        iterator = t;
    }

    /**
     * @param t   t an iterator to be transformed into a fluent one
     * @param <T> any type
     * @return t as fluent iterator
     */
    public static <T> FluentIterator<T> fluent(Iterator<T> t) {
        return new FluentIterator<>(t);
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public T next() {
        return iterator.next();
    }

    /**
     * @param s an iterator to be compared to this
     * @param n the number of elements to be compared
     * @return true iff the first n elements of s and this are weakEqual.
     */
    public boolean weakEquals(Iterator<T> s, int n) {
        return Iterators.weakEquals(this, s, n);
    }

    /**
     * @param p the predicate to be applied
     * @return an iterator containing all elements satisfying p
     */
    @SuppressWarnings("unchecked")
    public FluentIterator<T> filter(Predicate<? super T> p) {
        return fluent(Iterators.filter(this, p));
    }

    /**
     * @param p the predicate to be applied
     * @return true if at least one element of this satisfies p
     */
    public boolean any(Predicate<? super T> p) {
        return Iterators.any(this, p);
    }

    /**
     * @param p the predicate to be applied
     * @return true if all elements of this satisfy p
     */
    public boolean all(Predicate<? super T> p) {
        return Iterators.all(this, p);
    }

    /**
     * @param ts an iterator of iterators
     * @return the concatenation of this and all iterators of ts
     */
    @SuppressWarnings("unchecked")
    public FluentIterator<T> concat(Iterator... ts) {
        Iterator<T>[] tmp = Arrays.copyOf(ts, ts.length + 1);
        tmp[0] = this;
        System.arraycopy(ts, 0, tmp, 1, ts.length);
        return fluent(Iterators.concat(tmp));
    }


    /**
     * @param cs the consumer to be applied
     * @return an iterator which applies cs to each object before returning it
     */
    public FluentIterator<T> peek(Consumer<? super T> cs) {
        return fluent(Iterators.peek(this, cs));
    }

    /**
     * @param n the number of elements to be read
     * @return a list containing the first n elements of t
     * or all elements of t if there are not more than n.
     */
    public List<T> take(int n) {
        return Iterators.take(this, n);
    }

    /**
     * @return a list containing all elements of t
     * Warning: infinite iterators yield an infinite loop
     */
    public List<T> takeAll() {
        return Iterators.takeAll(this);
    }


    /**
     * @param n the number of elements to be considered
     * @return an iterator containing at most n elements
     */
    public FluentIterator<T> limit(int n) {
        return fluent(Iterators.limit(this, n));
    }


    /**
     * @param n the number of forks
     * @return list of n iterator clones to be iterated on independently
     * After calling tee, this must not be iterated on!
     */
    public List<FluentIterator<T>> tee(int n) {
        Function<Iterator<T>, FluentIterator<T>> f = FluentIterator::fluent;
        return Iterators.takeAll(Iterators.map(Iterators.tee(this, n).iterator(), f));
    }


    /**
     * @param <S> any type
     * @param s   an iterator to be zipped to this
     * @return an iterator of pairs
     */
    public <S> FluentIterator<Pair<T, S>> zip(Iterator<S> s, boolean weak) {
        Pair<Iterator<T>, Iterator<S>> p = pairOf((Iterator<T>) this, s);
        return fluent(Iterators.zip(p, weak));
    }


    /**
     * This function groups an iterator by a key function:
     * consecutive elements with the same key value form a group.
     * The result is an Iterator of Lists of objects
     * Example: 4 7 10 5 8 11 3, key(x) = x%3 produces ((4 7 10) (5 8 11) (3))
     *
     * @param key the key function controlling the grouping
     * @param <K> any type
     * @return the grouped iterator
     */
    public <K> FluentIterator<List<T>> groupBy(Function<T, K> key) {
        return fluent(Iterators.groupBy(this, key));
    }


    /**
     * This function groups an iterator.
     * consecutive elements which are equal form a group.
     * The result is an Iterator of List of objects.
     * Example: x x x y y z produces ((x x x) (y y) (z))
     *
     * @return the grouped iterator
     */
    public FluentIterator<List<T>> groupBy() {
        return fluent(Iterators.groupBy(this, x -> x));
    }


    /**
     * @param key the function defining what duplicates are
     * @param <K> any type
     * @return an iterator skipping consecutive duplicates
     * Elements x, y are duplicates
     * iff weakEquals(key.apply(x), (key.apply(y)) is true.
     * Of n consecutive equal elements, the first of is retained.
     */
    public <K> FluentIterator<T> skipDuplicates(Function<T, K> key) {
        return fluent(Iterators.skipDuplicates(this, key));
    }


    /**
     * @return an iterator skipping consecutive duplicates
     * thus: 1 1 2 2 3 3 3 4 produces 1 2 3 4
     * but:  1 2 3 4 1 2 3 4 produces 1 2 3 4 1 2 3 4
     * Of n consecutive equal elements, the first of is retained.
     * Warning:    If the input iterator contains indefinitely many
     * consecutive equal elements, next() will loop indefinitely
     */
    public FluentIterator<T> skipDuplicates() {
        return fluent(Iterators.skipDuplicates(this, x -> x));
    }


    /**
     * @param key the function defining what duplicates are
     * @return an iterator skipping consecutive duplicates
     * Elements x, y are duplicates
     * iff weakEquals(key.apply(x), (key.apply(y)) is true.
     * Of n consecutive equal elements, the first one is retained.
     */
    public <K> FluentIterator<T> keepFirst(Function<? super T, ? extends K> key) {
        return fluent(new SkipDuplicates<>(this, key, true));
    }

    /**
     * @param key the function defining what duplicates are
     * @return an iterator skipping consecutive duplicates
     * Elements x, y are duplicates
     * iff weakEquals(key.apply(x), (key.apply(y)) is true.
     * Of n consecutive equal elements, the last one is retained.
     */
    public <K> FluentIterator<T> keepLast(Function<? super T, ? extends K> key) {
        return fluent(new SkipDuplicates<>(this, key, false));
    }

    /**
     * @param f   the function to be applied to each element
     * @param <V> the type to which the elements of this will be mapped
     * @return the iterator of mapped elements
     */
    public <V extends Comparable<V>> FluentIterator<V> map(Function<T, V> f) {
        return fluent(Iterators.map(this, f));
    }


    /**
     * @param t the second iterator to be mapped, may contain nulls
     * @param f the function to be applied to each pair of elements from this and t
     * @return the iterator of mapped elements
     */
    public FluentIterator<T> map(Iterator<T> t, BinaryOperator<T> f) {
        return fluent(Iterators.map(this, t, f));
    }


    /**
     * @param f accumulator: the operator to be applied
     * @return the result
     */
    public T reduce(BinaryOperator<T> f) {
        return Iterators.reduce(this, f);
    }


    /**
     * @param f     the accumulator function
     * @param start the start value
     * @param <V>   any type
     * @return the accumulated result
     */
    public <V> V reduce(BiFunction<V, T, V> f, V start) {
        return Iterators.reduce(this, f, start);
    }
}
