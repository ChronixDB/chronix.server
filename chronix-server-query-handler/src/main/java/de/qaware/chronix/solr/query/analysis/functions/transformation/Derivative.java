/*
 * Copyright (C) 2016 QAware GmbH
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
package de.qaware.chronix.solr.query.analysis.functions.transformation;

import de.qaware.chronix.solr.query.analysis.functions.ChronixTransformation;
import de.qaware.chronix.solr.query.analysis.functions.FunctionType;
import de.qaware.chronix.timeseries.MetricTimeSeries;

/**
 * The derivative transformation
 *
 * @author f.lautenschlager
 */
public class Derivative implements ChronixTransformation<MetricTimeSeries> {
    /**
     * Calculates the derivative of the values per second.
     * Returns a time series holding that values.
     *
     * @param timeSeries the time series that is transformed
     * @return the time series with the derivative values
     */
    @Override
    public MetricTimeSeries transform(MetricTimeSeries timeSeries) {

        //we need a sorted time series
        timeSeries.sort();

        long[] times = timeSeries.getTimestampsAsArray();
        double[] values = timeSeries.getValuesAsArray();

        //Clear the time series
        timeSeries.clear();

        for (int i = 1; i < values.length - 1; i++) {

            long yT1 = times[i + 1];
            long yT0 = times[i - 1];

            double xT1 = values[i + 1];
            double xT0 = values[i - 1];

            double derivativeValue = calc(xT1, xT0, yT1, yT0);
            //We use the average time of
            long derivativeTime = yT1 + (yT1 - yT0) / 2;

            timeSeries.add(derivativeTime, derivativeValue);
        }

        return timeSeries;
    }

    /**
     * Calculates derivative of two points of a time series.
     * It uses the following algorithm:
     * <p>
     * ____________x{t+1}-x{t-1}
     * derivative = -----------
     * ____________2 * delta(t)
     *
     * @param xT1 the next value
     * @param xT  the current value
     * @param yT1 the next timestamp
     * @param yT  the current timestamp
     * @return the derivative value
     */
    private double calc(double xT1, double xT, long yT1, long yT) {
        long deltaTinSeconds = (yT1 - yT) / 1000;
        return (xT1 - xT) / (2 * deltaTinSeconds);
    }

    @Override
    public FunctionType getType() {
        return FunctionType.DERIVATIVE;
    }


    @Override
    public String[] getArguments() {
        return new String[0];
    }
}
