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
package de.qaware.chronix.solr.query.analysis.collectors.aggregation;

import de.qaware.chronix.solr.query.analysis.collectors.AnalysisType;
import de.qaware.chronix.solr.query.analysis.collectors.ChronixAnalysis;
import de.qaware.chronix.timeseries.MetricTimeSeries;

/**
 * Percentile aggregation analysis
 *
 * @author f.lautenschlager
 */
public final class Percentile implements ChronixAnalysis {


    private final double percentile;

    /**
     * Constructs a percentile aggregation
     *
     * @param percentile the percentile [0.0 ... 1.0]
     */
    public Percentile(double percentile) {
        this.percentile = percentile;
    }


    /**
     * Calculates the percentile of the first time series.
     *
     * @param args the time series
     * @return the percentile or 0 if the list is empty
     */
    @Override
    public double execute(MetricTimeSeries... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Percentile aggregation needs at least one time series");
        }

        MetricTimeSeries timeSeries = args[0];
        return de.qaware.chronix.solr.query.analysis.collectors.math.Percentile.evaluate(timeSeries.getValues(), percentile);
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{percentile};
    }

    @Override
    public AnalysisType getType() {
        return AnalysisType.P;
    }

    @Override
    public boolean needSubquery() {
        return false;
    }

    @Override
    public String getSubquery() {
        return null;
    }


}
