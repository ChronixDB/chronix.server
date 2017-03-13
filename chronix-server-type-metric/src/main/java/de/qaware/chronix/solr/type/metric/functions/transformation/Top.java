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
package de.qaware.chronix.solr.type.metric.functions.transformation;

import de.qaware.chronix.server.functions.ChronixTransformation;
import de.qaware.chronix.server.functions.FunctionValueMap;
import de.qaware.chronix.solr.type.metric.functions.math.NElements;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Top transformation to get value top values
 *
 * @author f.lautenschlager
 */
public final class Top implements ChronixTransformation<MetricTimeSeries> {

    private final int value;

    /**
     * Constructs the top value transformation
     *
     * @param args number of largest values that are returned
     */
    public Top(String[] args) {
        this.value = Integer.parseInt(args[0]);
    }

    @Override
    public void execute(MetricTimeSeries timeSeries, FunctionValueMap functionValueMap) {

        //Calculate the largest values
        NElements.NElementsResult result = NElements.calc(
                NElements.NElementsCalculation.TOP,
                value,
                timeSeries.getTimestampsAsArray(),
                timeSeries.getValuesAsArray());

        //remove the old time series values
        timeSeries.clear();
        //set the new top largest values
        timeSeries.addAll(result.getNTimes(), result.getNValues());
        functionValueMap.add(this);
    }

    @Override
    public String getQueryName() {
        return "top";
    }

    @Override
    public String getTimeSeriesType() {
        return "metric";
    }

    @Override
    public String[] getArguments() {
        return new String[]{"value=" + value};
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
                .append(this.value, rhs.value)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(value)
                .toHashCode();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("value", value)
                .toString();
    }
}
