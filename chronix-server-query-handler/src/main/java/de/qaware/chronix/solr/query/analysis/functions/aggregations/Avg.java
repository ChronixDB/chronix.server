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
package de.qaware.chronix.solr.query.analysis.functions.aggregations;

import de.qaware.chronix.solr.query.analysis.FunctionValueMap;
import de.qaware.chronix.solr.query.analysis.functions.ChronixAggregation;
import de.qaware.chronix.solr.query.analysis.functions.FunctionType;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author f.lautenschlager
 */
public final class Avg implements ChronixAggregation<MetricTimeSeries> {

    /**
     * Calculates the average value of the first time series.
     *
     * @param analysisAndValues
     * @return the average or 0 if the list is empty
     */
    @Override
    public void execute(MetricTimeSeries timeSeries, FunctionValueMap analysisAndValues) {

        //If it is empty, we return NaN
        if (timeSeries.size() <= 0) {
            analysisAndValues.add(this,Double.NaN);
            return;
        }

        //Else calculate the analysis value
        int size = timeSeries.size();
        double current = 0;
        for (int i = 0; i < size; i++) {
            current += timeSeries.getValue(i);
        }
        analysisAndValues.add(this, current / timeSeries.size());
    }

    @Override
    public String[] getArguments() {
        return new String[0];
    }

    @Override
    public FunctionType getType() {
        return FunctionType.AVG;
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
