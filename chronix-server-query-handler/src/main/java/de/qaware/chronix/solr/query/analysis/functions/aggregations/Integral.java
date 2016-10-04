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
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;

/**
 * Integral aggregation
 *
 * @author f.lautenschlager
 */
public final class Integral implements ChronixAggregation<MetricTimeSeries> {

    /**
     * Calculates the integral of the given time series using the simpson integrator of commons math lib
     *
     * @param timeSeries       the time series as argument for the chronix function
     * @param functionValueMap the analysis and values result map
     */
    @Override
    public void execute(MetricTimeSeries timeSeries, FunctionValueMap functionValueMap) {

        if (timeSeries.isEmpty()) {
            functionValueMap.add(this, Double.NaN);
            return;
        }

        SimpsonIntegrator simpsonIntegrator = new SimpsonIntegrator();
        double integral = simpsonIntegrator.integrate(Integer.MAX_VALUE, x -> timeSeries.getValue((int) x), 0, timeSeries.size() - 1);

        functionValueMap.add(this, integral);
    }

    @Override
    public FunctionType getType() {
        return FunctionType.INTEGRAL;
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
