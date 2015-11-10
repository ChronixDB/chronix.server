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
import de.qaware.chronix.dts.WeakLogic;
import de.qaware.chronix.iterators.FluentIterator;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static de.qaware.chronix.dts.Pair.pairOf;
import static de.qaware.chronix.dts.WeakLogic.weakBinaryOperator;
import static de.qaware.chronix.dts.WeakLogic.weakComparator;
import static de.qaware.chronix.iterators.FluentIterator.fluent;
import static de.qaware.chronix.iterators.Iterators.*;
import static java.util.Arrays.asList;
import static java.util.Collections.binarySearch;

/**
 * A time series is a list of timestamps
 * Each timestamp is a (time, value)-pair
 * This class guarantees:
 * a) argument of first timestamp =  -oo (represented by null)
 * b) timestamps strictly ascending by argument
 * c) value change at each timestamp
 *
 * @param <T> type of time axis
 * @param <V> type of value axis
 * @author johannes.siedersleben
 */
public class TimeSeries<T extends Comparable<T>, V> implements Function<T, V>, Iterable<Pair<T, V>> {

    private List<T> times = new ArrayList<>();
    private List<V> values = new ArrayList<>();
    private Comparator<T> cmp = weakComparator();
    private Map<String, Object> attributes = new HashMap<>();

    /**
     * @param timestamps an iterator of (time, value)-pairs.
     *                   identical times are discarded but the last.
     *                   identical values are discarded but the first.
     *                   Timestamps must be non-descending
     *                   A timestamp (null, null) will be inserted if missing
     */
    public TimeSeries(Iterator<Pair<T, V>> timestamps) {
        FluentIterator<Pair<T, V>> aux =
                fluent(of(pairOf((T) null, (V) null)))
                        .concat(timestamps)
                        .keepLast(Pair::getFirst)
                        .keepFirst(Pair::getSecond);

        while (aux.hasNext()) {
            Pair<T, V> current = aux.next();
            times.add(current.getFirst());
            values.add(current.getSecond());
        }
    }

    /**
     * @param timestamps an iterator of (time, value)-pairs.
     *                   identical times are discarded but the last.
     *                   identical values are discarded but the first.
     *                   Timestamps must be non-descending
     *                   A timestamp (null, null) will be inserted if missing
     */
    public TimeSeries(Iterable<Pair<T, V>> timestamps) {
        this(timestamps.iterator());
    }

    /**
     * @param t an iterator of non-descending timestamps
     * @param f a function mapping timestamps to values
     */
    public TimeSeries(Iterator<T> t, Function<T, V> f) {
        this(map(t, x -> pairOf(x, f.apply(x))));
    }

    /**
     * @param ts  a time series
     * @param <T> the time type
     * @param <V> the value type
     * @return a new time series unioning all time stamps given,
     * the value being the list of all values valid at that time
     */
    public static <T extends Comparable<T>, V>
    TimeSeries<T, List<V>> merge(Iterable<TimeSeries<T, V>> ts) {
        Iterator<Pair<T, List<V>>> aux = new TimeSeriesMerge<>(map(asIterator(ts), Iterable::iterator));
        return new TimeSeries<>(aux);
    }

    /**
     * @param ts  a time series
     * @param <T> the time type
     * @param <V> the value type
     * @param op  an operator reducing the value list
     * @return a new time series unioning all time stamps given,
     * the value being the result of the reducing by op (e.g. min, max, avg)
     */
    public static <T extends Comparable<T>, V>
    TimeSeries<T, V> merge(Iterable<TimeSeries<T, V>> ts, BinaryOperator<V> op) {
        BinaryOperator<V> wop = weakBinaryOperator(op);
        Iterator<Pair<T, List<V>>> aux1 = new TimeSeriesMerge<>(map(asIterator(ts), Iterable::iterator));
        Iterator<Pair<T, V>> aux2 =
                map(aux1, (Pair<T, List<V>> p) -> pairOf(p.getFirst(), reduce(asIterator(p.getSecond()), wop)));
        return new TimeSeries<>(aux2);
    }

    /**
     * @param tv  the first time series
     * @param tw  the second time series
     * @param f   a function mapping V x V -> U    (e.g. x, y -> x <= y)
     * @param <T> type of timestamps
     * @param <V> type of values
     * @param <U> return type of f
     * @return a new time series unioning all time stamps given,
     * the value being the result of f (e.g. lessThan, equals)
     */
    public static <T extends Comparable<T>, V, U>
    TimeSeries<T, U> merge(TimeSeries<T, V> tv, TimeSeries<T, V> tw, BiFunction<V, V, U> f) {
        Iterator<Pair<T, List<V>>> aux1 = asIterator(merge(asList(tv, tw)));
        Iterator<Pair<T, U>> aux2 = map(aux1, (Pair<T, List<V>> p) ->
                pairOf(p.getFirst(), f.apply(p.getSecond().get(0), p.getSecond().get(1))));
        return new TimeSeries<>(aux2);
    }

    /**
     * @return the number of timestamps of this time series.
     */
    public int size() {
        return times.size();
    }

    /**
     * @param i the index
     * @return the timestamp at i
     */
    public Pair<T, V> get(int i) {
        return pairOf(times.get(i), values.get(i));
    }

    /**
     * @param t an iterator of non-descending timestamps
     * @return a timeSeries identical to this but relocated to t
     */
    public TimeSeries<T, V> relocate(Iterator<T> t) {
        return new TimeSeries<>(t, this);
    }

    /**
     * @param x the argument
     * @return the value of this at x
     */
    public V apply(T x) {
        int i = binarySearch(times, x, cmp);
        i = (0 <= i) ? i : -i - 2;
        return get(i).getSecond();
    }


    /**
     * @param a left border   a <= b
     * @param b right border
     * @return returns true if this is constant on [a, b)
     */
    public boolean sameLeg(T a, T b) {
        if (cmp.compare(a, b) > 0) {
            throw new IllegalArgumentException();
        }
        int i = binarySearch(times, a, cmp);
        i = (0 <= i) ? i : -i - 2;
        int j = binarySearch(times, b, cmp);
        int j1 = (0 <= j) ? j : -j - 2;
        return i == j1 || (j >= 0 && i + 1 == j);
    }


    /**
     * @param a left border   a <= b
     * @param b right border
     * @return returns a time series identical with this on [a, b) and undefined otherwise
     */
    public TimeSeries<T, V> subSeries(T a, T b) {
        if (cmp.compare(a, b) >= 0) {
            throw new IllegalArgumentException();
        }
        int i = binarySearch(times, a, cmp);
        i = (0 <= i) ? i : -i - 2;
        int j = binarySearch(times, b, cmp);
        j = (0 <= j) ? j - 1 : -j - 2;
        Iterator<Pair<T, V>> result = fluent(of(pairOf(a, this.apply(a))))
                .concat(zip(pairOf(times.subList(i, j + 1).iterator(), values.subList(i, j + 1).iterator()), true),
                        of(pairOf(b, null)));
        return new TimeSeries<>(result);
    }

    /**
     * @return an iterator yielding all (step, value)-pairs
     */
    public Iterator<Pair<T, V>> iterator() {
        return zip(pairOf(times.iterator(), values.iterator()), true);
    }

    /**
     * Adds the given attribute and value
     *
     * @param attribute - the attribute
     * @param value     - the value
     */
    public void addAttribute(String attribute, Object value) {
        attributes.put(attribute, value);
    }

    /**
     * Gets the value fo the given attribute
     *
     * @param attribute - the attribute
     * @return the value
     */
    public Object getAttribute(String attribute) {
        return attributes.get(attribute);
    }

    /**
     * @return an iterator over the attributes
     */
    public Iterator<Map.Entry<String, Object>> getAttributes() {
        return attributes.entrySet().iterator();
    }

    @Override
    public boolean equals(Object x) {
        if (this == x) {
            return true;
        }
        if (!(x instanceof TimeSeries)) {
            return false;
        }

        TimeSeries that = (TimeSeries) x;

        BiFunction<V, V, Boolean> f = WeakLogic::weakEquals;
        TimeSeries aux = merge(this, that, f);
        return Boolean.TRUE.equals(aux.apply(null)) && (aux.size() == 1);
    }

    @Override
    public int hashCode() {
        return times.hashCode() + values.hashCode();
    }


}
