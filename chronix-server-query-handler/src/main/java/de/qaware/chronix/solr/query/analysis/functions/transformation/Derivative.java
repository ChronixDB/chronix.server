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
import de.qaware.chronix.solr.query.analysis.functions.FunctionValueMap;
import de.qaware.chronix.solr.query.analysis.functions.math.DerivativeUtil;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * The derivative transformation
 *
 * @author f.lautenschlager
 */
public final class Derivative implements ChronixTransformation<MetricTimeSeries> {
    /**
     * Calculates the derivative of the values per second.
     * Returns a time series holding that values.
     *
     * @param timeSeries the time series that is transformed
     */
    @Override
    public void execute(MetricTimeSeries timeSeries, FunctionValueMap functionValueMap) {

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

            double derivativeValue = DerivativeUtil.derivative(xT1, xT0, yT1, yT0);
            //We use the average time of
            long derivativeTime = yT1 + (yT1 - yT0) / 2;

            timeSeries.add(derivativeTime, derivativeValue);
        }
        functionValueMap.add(this);
    }


    @Override
    public FunctionType getType() {
        return FunctionType.DERIVATIVE;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        return new EqualsBuilder()
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .toHashCode();
    }

}
