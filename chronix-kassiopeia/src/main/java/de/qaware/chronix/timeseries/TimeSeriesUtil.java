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

import java.util.Iterator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static de.qaware.chronix.dts.Pair.pairOf;
import static de.qaware.chronix.dts.WeakLogic.weakBinaryOperator;
import static de.qaware.chronix.iterators.FluentIterator.fluent;
import static de.qaware.chronix.iterators.Iterators.asIterator;
import static de.qaware.chronix.iterators.Iterators.map;
import static de.qaware.chronix.iterators.Iterators.reduce;


/**
 * This class contains some utility functions for dealing with Iterables or Iterators of
 * (time, value)-pairs.
 *
 * @author johannes.siedersleben
 */
public final class TimeSeriesUtil {

    /**
     * Private utility class constructor.
     */
    private TimeSeriesUtil() {
    }

    /**
     * Cleansing means:
     * keep the last timeValuePair of identical timestamps and skip all others
     * keep the first timeValuePair of consecutive pairs with identical values
     * and skip all others
     *
     * @param input the time series to be cleansed
     * @param <T>   type of time
     * @param <V>   type of value
     * @return the cleansed time series
     */
    public static <T extends Comparable<T>, V> Iterator<Pair<T, V>> cleanse(Iterator<Pair<T, V>> input) {
        return fluent(input)
                .keepLast(Pair::getFirst)
                .keepFirst(Pair::getSecond);
    }


    /**
     * This method merges n time series into one according to the following strategy:
     * The timestamps are merged; the resulting sequence is the union of the given ones.
     * The value is the list of all input values at that timestamp
     *
     * @param input an iterable of time series to be merged
     * @param <T>   the time type
     * @param <V>   type of input values
     * @return iterator of merged pairs of (time, list of values))
     */
    public static <T extends Comparable<T>, V> Iterator<Pair<T, List<V>>>
    merge(Iterable<Iterator<Pair<T, V>>> input) {
        return new TimeSeriesMerge<>(input);
    }


    /**
     * This method merges n time series into one according to the following strategy:
     * The timestamps are merged; the resulting sequence is the union of the given ones.
     * On each timestamp, the new value is computed by reducing the given operator to
     * the vector of values at that time stamp.
     * Typical operators are min, max, sum, avg.
     *
     * @param input an iterable of time series to be merged
     * @param op    the operator to be applied
     * @param <T>   type of timestamps
     * @param <V>   type of values
     * @return iterator of merged timeValuePairs
     */
    public static <T extends Comparable<T>, V> Iterator<Pair<T, V>>
    merge(Iterable<Iterator<Pair<T, V>>> input, BinaryOperator<V> op) {
        return map(merge(input),
                (Pair<T, List<V>> p) -> pairOf(p.getFirst(),
                        reduce(asIterator(p.getSecond()), weakBinaryOperator(op))));
    }




    /**
     * This method compacts a time series according to the following strategy:
     * The resulting time series has the timestamps given by samples s0, s1, ...
     * At each sample si the new value is computed by applying the compactor
     * to all input values on tj where si <= tj < si+1.
     * Typical compactors are max, min, sum, average.
     * Input values at timestamps < s0 are skipped.
     *
     * @param input     the time series to be compacted
     * @param samples   the timestamps to which the series is to be compacted to
     * @param compactor the compactor function
     * @param <T>       type of timestamps
     * @param <V>       type of values
     * @return iterator of compacted timeValuePairs
     */

    public static <T extends Comparable<T>, V, W> Iterator<Pair<T, W>>
    compact(Iterator<Pair<T, V>> input, Iterator<T> samples, Function<List<V>, W> compactor) {
        return new TimeSeriesCompact<>(input, samples, compactor);
    }


    /**
     * This method approximates the input by a stepwise linear function.
     * It returns an iterator of segments, each segment being defined by a timestamp (start of segment)
     * and pair (intercept, slope) which determines the time series from the start up to and
     * excluding the start of the next segment. The algorithm proceeds as follows:
     * Let n timeValuePairs be processed and alpha be the slope of their least square approximation.
     * By construction, the mean square error is smaller than epsilon.
     * We now add the (n+1)th timeValuePair and compute the resulting least square approximation.
     * This step is repeated until the mean square error remains exceeds epsilon.
     * If it does, the segment is stored as Pair(start of segment, Pair(intercept, alpha))
     * and the process starts over.
     *
     * @param input the time series to be linearized
     * @return a stepwise linear function approximating input by epsilon give by an iterator of
     * (start of segment, (intercept, slope))
     */

    public static Iterator<Pair<Double, Pair<Double, Double>>>
    linearize(Iterator<Pair<Double, Double>> input, double epsilon) {
        return new TimeSeriesRegression(input, epsilon);
    }
}
