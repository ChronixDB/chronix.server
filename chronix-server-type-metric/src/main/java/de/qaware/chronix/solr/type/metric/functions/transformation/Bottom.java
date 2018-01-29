/*
 * Copyright (C) 2018 QAware GmbH
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
import de.qaware.chronix.server.functions.FunctionCtx;
import de.qaware.chronix.server.types.ChronixTimeSeries;
import de.qaware.chronix.solr.type.metric.functions.math.NElements;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * Bottom transformation get the value bottom values
 *
 * @author f.lautenschlager
 */
public final class Bottom implements ChronixTransformation<MetricTimeSeries> {

    private int value;

    @Override
    public void execute(List<ChronixTimeSeries<MetricTimeSeries>> timeSeriesList, FunctionCtx functionCtx) {

        for (ChronixTimeSeries<MetricTimeSeries> chronixTimeSeries : timeSeriesList) {
            MetricTimeSeries timeSeries = chronixTimeSeries.getRawTimeSeries();

            NElements.NElementsResult result = NElements.calc(NElements.NElementsCalculation.BOTTOM, value, timeSeries.getTimestampsAsArray(), timeSeries.getValuesAsArray());

            //remove old time series
            timeSeries.clear();
            timeSeries.addAll(result.getNTimes(), result.getNValues());
            functionCtx.add(this, chronixTimeSeries.getJoinKey());
        }
    }


    @Override
    public String getQueryName() {
        return "bottom";
    }

    @Override
    public String getType() {
        return "metric";
    }

    /**
     * @param args the first parameter is the threshold for the lowest values
     */
    @Override
    public void setArguments(String[] args) {
        this.value = Integer.parseInt(args[0]);
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
        Bottom rhs = (Bottom) obj;
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
