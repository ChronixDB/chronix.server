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
import de.qaware.chronix.iterators.ImmutableIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * A time series compactor implementation.
 *
 * @author johannes.siedersleben
 */
class TimeSeriesCompact<T extends Comparable<T>, V, W> implements ImmutableIterator<Pair<T, W>> {

    private final Iterator<T> samples;
    private final Iterator<Pair<T, V>> input;
    private final Function<List<V>, W> compactor;

    private T currentSample;                      // the sample values are being compacted to
    private Pair<T, V> currentInput;
    private List<V> buffer = new ArrayList<>();   // collected input values to be compacted

    /**
     * @param input     iterator to be compacted, may be empty;
     *                  ascending T-values assumed
     * @param samples   samples the input is compacted to.
     *                  ascending T-values assumed
     *                  input before first sample is skipped;
     *                  input must stop before last sample.
     * @param compactor the compactor (sum, average, max, min, ...)
     */
    public TimeSeriesCompact(Iterator<Pair<T, V>> input, Iterator<T> samples, Function<List<V>, W> compactor) {
        this.input = input;
        this.samples = samples;
        this.currentSample = samples.next();

        while (input.hasNext()) {                 // ignore input up to excluding first sample
            currentInput = input.next();
            if (currentInput.getFirst().compareTo(currentSample) >= 0) {
                buffer.add(currentInput.getSecond());   // prepare first call of next
                break;
            }
        }

        this.compactor = compactor;
    }

    @Override
    public boolean hasNext() {
        return input.hasNext() || buffer.size() > 0;
    }


    // precondition:
    // currentSample is properly set; buffer contains first input with t >= currentSample
    @Override
    public Pair<T, W> next() {

        T nextSample = samples.next();

        while (input.hasNext()) {                       // unwind input until nextSample
            currentInput = input.next();
            if (currentInput.getFirst().compareTo(nextSample) < 0) {
                buffer.add(currentInput.getSecond());   // collect values in buffer
            } else {                                    // current chunk is done
                W value = compactor.apply(buffer);      // compute result
                Pair<T, W> result = new Pair<>(currentSample, value);
                buffer = new ArrayList<>();             // prepare buffer for next call
                buffer.add(currentInput.getSecond());
                currentSample = nextSample;             // update currentSample
                return result;                          // break while
            }
        }

        W value = compactor.apply(buffer);              // dispatch last chunk
        Pair<T, W> result = new Pair<>(currentSample, value);
        buffer = new ArrayList<>();
        return result;
    }
}
