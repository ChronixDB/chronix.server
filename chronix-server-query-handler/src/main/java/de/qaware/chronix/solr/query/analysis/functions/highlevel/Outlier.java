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
package de.qaware.chronix.solr.query.analysis.functions.highlevel;

import de.qaware.chronix.solr.query.analysis.functions.AnalysisType;
import de.qaware.chronix.solr.query.analysis.functions.ChronixAnalysis;
import de.qaware.chronix.solr.query.analysis.functions.math.Percentile;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import de.qaware.chronix.timeseries.dt.DoubleList;

/**
 * The outlier analysis
 *
 * @author f.lautenschlager
 */
public class Outlier implements ChronixAnalysis<MetricTimeSeries> {

    /**
     * Detects outliers using the default box plot implementation.
     * An outlier every value that is above (q3-q1)*1.5*q3 where qN is the nth percentile
     *
     * @param args the time series
     * @return 1 if there are outliers, otherwise -1
     */
    @Override
    public double execute(MetricTimeSeries... args) {
        if (args.length <= 0) {
            throw new IllegalArgumentException("Trend detection needs at least one time series");
        }

        MetricTimeSeries timeSeries = args[0];

        if (timeSeries.isEmpty()) {
            return -1;
        }

        DoubleList points = timeSeries.getValues();
        //Calculate the percentiles
        double q1 = Percentile.evaluate(points, .25);
        double q3 = Percentile.evaluate(points, .75);
        //Calculate the threshold
        double threshold = (q3 - q1) * 1.5 + q3;
        //filter the values, if one outlier is found, we can return
        for (int i = 0; i < points.size(); i++) {
            double point = points.get(i);
            if (point > threshold) {
                return 1;
            }
        }
        return -1;
    }

    @Override
    public String[] getArguments() {
        return new String[0];
    }

    @Override
    public AnalysisType getType() {
        return AnalysisType.OUTLIER;
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
