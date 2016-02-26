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
package de.qaware.chronix.solr.query.analysis.functions;

import de.qaware.chronix.timeseries.MetricTimeSeries;

/**
 * The minimum aggregation
 *
 * @author f.lautenschlager
 */
public class Min implements ChronixAnalysis {

    /**
     * Calculates the minimum value of the first time series.
     *
     * @param args the time series for this analysis
     * @return the minimum or 0 if the list is empty
     */
    @Override
    public double execute(MetricTimeSeries... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Min aggregation needs at least one time series");
        }

        MetricTimeSeries timeSeries = args[0];
        double min = 0;
        if (timeSeries.size() <= 0) {
            return min;
        }

        for (int i = 0; i < timeSeries.size(); i++) {
            double next = timeSeries.getValue(i);
            if (min > next) {
                min = next;
            }
        }
        return min;
    }

    @Override
    public String[] getArguments() {
        return new String[0];
    }

    @Override
    public AnalysisType getType() {
        return AnalysisType.MIN;
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
