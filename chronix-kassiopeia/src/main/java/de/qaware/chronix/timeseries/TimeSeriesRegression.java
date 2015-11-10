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
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.Iterator;

/**
 * A time series regression implementation.
 *
 * @author johannes.siedersleben
 */
class TimeSeriesRegression implements ImmutableIterator<Pair<Double, Pair<Double, Double>>> {

    private Iterator<Pair<Double, Double>> input;
    private double epsilon;
    private Double start;                           // start of current segment
    private Double intercept;                       // intercept of current segment
    private Double slope = 0.0;                     // slope of current segment
    private SimpleRegression regression = new SimpleRegression();


    /**
     * @param input iterator to be linearized
     */
    public TimeSeriesRegression(Iterator<Pair<Double, Double>> input, double epsilon) {
        this.input = input;
        this.epsilon = epsilon;
        if (input.hasNext()) {
            Pair<Double, Double> currentInput = input.next();
            start = currentInput.getFirst();
            intercept = currentInput.getSecond();
            regression.addData(0.0, intercept);
        }
    }

    @Override
    public boolean hasNext() {
        return input.hasNext() || regression.getN() > 0;
    }


    // preconditions:
    // start is start of current segment
    // slope is slope of current segment
    // intercept is intercept of current segment
    @Override
    public Pair<Double, Pair<Double, Double>> next() {
        while (input.hasNext()) {

            Pair<Double, Double> currentInput = input.next();
            regression.addData(currentInput.getFirst() - start, currentInput.getSecond());

            if (regression.getMeanSquareError() >= epsilon) {    // start new segment
                regression.clear();

                Pair<Double, Double> value = Pair.pairOf(intercept, slope);
                Pair<Double, Pair<Double, Double>> result = Pair.pairOf(start, value);

                start = currentInput.getFirst();
                intercept = currentInput.getSecond();
                slope = 0.0;
                regression.addData(0.0, intercept);

                return result;

            } else {                                            // continue current segment
                intercept = regression.getIntercept();
                slope = regression.getSlope();
            }
        }

        regression.clear();

        Pair<Double, Double> value = Pair.pairOf(intercept, slope);
        return Pair.pairOf(start, value);
    }
}
