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
import de.qaware.chronix.solr.query.analysis.functions.math.NElements;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Top transformation to get n top values
 *
 * @author f.lautenschlager
 */
public class Top implements ChronixTransformation<MetricTimeSeries> {

    private final int n;

    /**
     * Constructs the top n transformation
     *
     * @param n values that are returned
     */
    public Top(int n) {
        this.n = n;
    }

    @Override
    public MetricTimeSeries transform(MetricTimeSeries timeSeries) {
        NElements.NElementsResult result = NElements.calc(NElements.NElementsCalculation.TOP, n, timeSeries.getTimestampsAsArray(), timeSeries.getValuesAsArray());

        //remove old time series
        timeSeries.clear();
        timeSeries.addAll(result.getNTimes(), result.getNValues());

        return timeSeries;
    }

    @Override
    public FunctionType getType() {
        return FunctionType.TOP;
    }

    @Override
    public String[] getArguments() {
        return new String[]{"n=" + n};
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
        Top rhs = (Top) obj;
        return new EqualsBuilder()
                .append(this.n, rhs.n)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(n)
                .toHashCode();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("n", n)
                .toString();
    }
}
