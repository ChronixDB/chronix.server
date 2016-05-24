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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Scale transformation
 *
 * @author f.lautenschlager
 */
public class Scale implements ChronixTransformation<MetricTimeSeries> {

    private final double scale;

    /**
     * Scales the time series by the given factor
     *
     * @param scale the scale factor
     */
    public Scale(double scale) {
        this.scale = scale;
    }

    @Override
    public MetricTimeSeries transform(MetricTimeSeries timeSeries) {

        double[] values = timeSeries.getValuesAsArray();
        long[] times = timeSeries.getTimestampsAsArray();
        for (int i = 0; i < timeSeries.size(); i++) {
            values[i] = values[i] * scale;
        }

        timeSeries.clear();
        timeSeries.addAll(times, values);

        return timeSeries;
    }

    @Override
    public FunctionType getType() {
        return FunctionType.SCALE;
    }

    @Override
    public String[] getArguments() {
        return new String[]{"scale=" + scale};
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
        Scale rhs = (Scale) obj;
        return new EqualsBuilder()
                .append(this.scale, rhs.scale)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(scale)
                .toHashCode();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("scale", scale)
                .toString();
    }
}
