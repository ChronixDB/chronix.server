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
package de.qaware.chronix.solr.query.analysis.collectors.analysis;

import de.qaware.chronix.solr.query.analysis.collectors.AnalysisType;
import de.qaware.chronix.solr.query.analysis.collectors.ChronixAnalysis;
import de.qaware.chronix.solr.query.analysis.collectors.math.LinearRegression;
import de.qaware.chronix.timeseries.MetricTimeSeries;

/**
 * The trend analysis
 *
 * @author f.lautenschlager
 */
public final class Trend implements ChronixAnalysis {
    /**
     * Detects trends in time series using a linear regression.
     *
     * @param args the time series
     * @return 1 if there is a positive trend, otherwise -1
     */
    @Override
    public double execute(MetricTimeSeries... args) {
        if (args.length <= 0) {
            throw new IllegalArgumentException("Trend detection needs at least one time series");
        }

        MetricTimeSeries timeSeries = args[0];
        timeSeries.sort();

        LinearRegression linearRegression = new LinearRegression(timeSeries.getTimestamps(), timeSeries.getValues());
        double slope = linearRegression.slope();
        return slope > 0 ? 1 : -1;
    }

    @Override
    public Object[] getArguments() {
        return new Object[0];
    }

    @Override
    public AnalysisType getType() {
        return AnalysisType.TREND;
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
