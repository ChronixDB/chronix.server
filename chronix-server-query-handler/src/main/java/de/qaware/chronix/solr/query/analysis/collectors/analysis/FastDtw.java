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

import de.qaware.chronix.distance.DistanceFunction;
import de.qaware.chronix.distance.DistanceFunctionEnum;
import de.qaware.chronix.distance.DistanceFunctionFactory;
import de.qaware.chronix.dtw.FastDTW;
import de.qaware.chronix.dtw.TimeWarpInfo;
import de.qaware.chronix.solr.query.analysis.collectors.AnalysisType;
import de.qaware.chronix.solr.query.analysis.collectors.ChronixAnalysis;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import de.qaware.chronix.timeseries.MultivariateTimeSeries;

/**
 * The analysis implementation of the Fast DTW analysis
 *
 * @author f.lautenschlager
 */
public final class FastDtw implements ChronixAnalysis {

    private final DistanceFunction distanceFunction;
    private final int searchRadius;
    private final double maxDifference;
    private final String subquery;

    public FastDtw(String subquery, int searchRadius, double maxDifference) {
        this.subquery = subquery;
        this.searchRadius = searchRadius;
        this.maxDifference = maxDifference;
        this.distanceFunction = DistanceFunctionFactory.getDistanceFunction(DistanceFunctionEnum.EUCLIDEAN);
    }

    @Override
    public double execute(MetricTimeSeries... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Fast DTW needs at least two time series");
        }

        MultivariateTimeSeries origin = buildMultiVariateTimeSeries(args[0]);
        MultivariateTimeSeries other = buildMultiVariateTimeSeries(args[1]);
        TimeWarpInfo result = FastDTW.getWarpInfoBetween(origin, other, searchRadius, distanceFunction);
        if (result.getNormalizedDistance() <= maxDifference) {
            return result.getNormalizedDistance();
        }
        return -1;
    }


    private MultivariateTimeSeries buildMultiVariateTimeSeries(MetricTimeSeries timeSeries) {
        MultivariateTimeSeries multivariateTimeSeries = new MultivariateTimeSeries(1);
        timeSeries.sort();
        timeSeries.points().forEachOrdered(pair -> {
            multivariateTimeSeries.add(pair.getTimestamp(), new double[]{pair.getValue()});
        });
        return multivariateTimeSeries;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{searchRadius, maxDifference, distanceFunction};
    }

    @Override
    public AnalysisType getType() {
        return AnalysisType.FASTDTW;
    }

    @Override
    public boolean needSubquery() {
        return true;
    }

    @Override
    public String getSubquery() {
        return subquery;
    }
}
