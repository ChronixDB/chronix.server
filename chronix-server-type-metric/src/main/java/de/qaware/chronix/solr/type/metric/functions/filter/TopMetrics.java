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
package de.qaware.chronix.solr.type.metric.functions.filter;

import com.carrotsearch.hppc.DoubleArrayList;
import de.qaware.chronix.server.functions.ChronixFilter;
import de.qaware.chronix.server.functions.ChronixTransformation;
import de.qaware.chronix.server.functions.FunctionCtx;
import de.qaware.chronix.server.types.ChronixTimeSeries;
import de.qaware.chronix.solr.type.metric.ChronixMetricTimeSeries;
import de.qaware.chronix.solr.type.metric.functions.math.NElements;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * The TopMetrics filter to get the 'value' top metrics
 *
 * @author k.just
 */
public final class TopMetrics implements ChronixFilter<MetricTimeSeries> {

    private int value;

    @Override
    public void execute(List<ChronixTimeSeries<MetricTimeSeries>> timeSeriesList, FunctionCtx functionCtx) {

        List<ChronixTimeSeries<MetricTimeSeries>> filteredList = new ArrayList<>();
        DoubleArrayList maxValueList = new DoubleArrayList();
        for (ChronixTimeSeries<MetricTimeSeries> chronixTimeSeries : timeSeriesList) {
            MetricTimeSeries timeSeries = chronixTimeSeries.getRawTimeSeries();

            // if list is not full yet, add series and it's max value
            if(filteredList.size() < value) {
                filteredList.add(chronixTimeSeries);
                maxValueList.add(NElements.calc(NElements.NElementsCalculation.TOP, 1, timeSeries.getTimestampsAsArray(), timeSeries.getValuesAsArray()).getNValues()[0]);
            }
            else
            {
                // find the index of the series with the smallest value
                int minIndex = 0;
                for(int i = 0; i < value; i++)
                    if(maxValueList.get(i) < maxValueList.get(minIndex))
                        minIndex = i;
                double competitorMaxVal = NElements.calc(NElements.NElementsCalculation.TOP, 1, timeSeries.getTimestampsAsArray(), timeSeries.getValuesAsArray()).getNValues()[0];

                // replace series with smallest value if competitor is greater
                if(maxValueList.get(minIndex) < competitorMaxVal){
                    maxValueList.remove(minIndex);
                    filteredList.remove(minIndex);
                    maxValueList.add(competitorMaxVal);
                    filteredList.add(chronixTimeSeries);
                }
            }
        }

        for (ChronixTimeSeries<MetricTimeSeries> chronixTimeSeries : timeSeriesList) {
            functionCtx.add(this, chronixTimeSeries.getJoinKey());
        }
    }

    @Override
    public String getQueryName() {
        return "topmetrics";
    }

    @Override
    public String getType() {
        return "metric";
    }


    /**
     * @param args number of metrics that are returned
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
        TopMetrics rhs = (TopMetrics) obj;
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