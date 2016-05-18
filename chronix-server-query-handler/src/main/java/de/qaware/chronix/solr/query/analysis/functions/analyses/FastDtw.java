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
package de.qaware.chronix.solr.query.analysis.functions.analyses;

import de.qaware.chronix.distance.DistanceFunction;
import de.qaware.chronix.distance.DistanceFunctionEnum;
import de.qaware.chronix.distance.DistanceFunctionFactory;
import de.qaware.chronix.dtw.FastDTW;
import de.qaware.chronix.dtw.TimeWarpInfo;
import de.qaware.chronix.solr.query.analysis.functions.ChronixAnalysis;
import de.qaware.chronix.solr.query.analysis.functions.FunctionType;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import de.qaware.chronix.timeseries.MultivariateTimeSeries;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * The analysis implementation of the Fast DTW analysis
 *
 * @author f.lautenschlager
 */
public final class FastDtw implements ChronixAnalysis<MetricTimeSeries> {

    private final DistanceFunction distanceFunction;
    private final int searchRadius;
    private final double maxNormalizedWarpingCost;
    private final String subquery;

    /**
     * @param subquery                 the subquery to describe the other set of time series
     * @param searchRadius             the search radius
     * @param maxNormalizedWarpingCost the maximum normalized maximum warping cost
     */
    public FastDtw(String subquery, int searchRadius, double maxNormalizedWarpingCost) {
        this.subquery = subquery;
        this.searchRadius = searchRadius;
        this.maxNormalizedWarpingCost = maxNormalizedWarpingCost;
        this.distanceFunction = DistanceFunctionFactory.getDistanceFunction(DistanceFunctionEnum.EUCLIDEAN);
    }

    @Override
    public boolean execute(MetricTimeSeries... args) {
        //We need two time series
        if (args.length < 2) {
            throw new IllegalArgumentException("Fast DTW needs at least two time series");
        }
        //We have to build a multivariate time series
        MultivariateTimeSeries origin = buildMultiVariateTimeSeries(args[0]);
        MultivariateTimeSeries other = buildMultiVariateTimeSeries(args[1]);
        //Call the fast dtw library
        TimeWarpInfo result = FastDTW.getWarpInfoBetween(origin, other, searchRadius, distanceFunction);
        //Check the result. If it lower equals the threshold, we can return the other time series
        return result.getNormalizedDistance() <= maxNormalizedWarpingCost;
    }

    /**
     * Builds a multivariate time series of the given univariate time series.
     * If two or more timestamps are the same, the values are aggregated using the average.
     *
     * @param timeSeries the metric time series
     * @return a multivariate time series for the fast dtw analysis
     */
    private MultivariateTimeSeries buildMultiVariateTimeSeries(MetricTimeSeries timeSeries) {
        MultivariateTimeSeries multivariateTimeSeries = new MultivariateTimeSeries(1);

        if (timeSeries.size() > 0) {
            //First sort the values
            timeSeries.sort();

            long formerTimestamp = timeSeries.getTime(0);
            double formerValue = timeSeries.getValue(0);
            int timesSameTimestamp = 0;

            for (int i = 1; i < timeSeries.size(); i++) {

                //We have two timestamps that are the same
                if (formerTimestamp == timeSeries.getTime(i)) {
                    formerValue += timeSeries.getValue(i);
                    timesSameTimestamp++;
                } else {
                    //calc the average of the values of the same timestamp
                    if (timesSameTimestamp > 0) {
                        formerValue = formerValue / timesSameTimestamp;
                        timesSameTimestamp = 0;
                    }
                    //first add the former timestamp
                    multivariateTimeSeries.add(formerTimestamp, new double[]{formerValue});
                    formerTimestamp = timeSeries.getTime(i);
                    formerValue = timeSeries.getValue(i);
                }
            }
            //add the last point
            multivariateTimeSeries.add(formerTimestamp, new double[]{formerValue});
        }

        return multivariateTimeSeries;
    }

    @Override
    public String[] getArguments() {
        return new String[]{"search radius=" + searchRadius,
                "max warping cost=" + maxNormalizedWarpingCost,
                "distance function=" + DistanceFunctionEnum.EUCLIDEAN.name()};
    }

    @Override
    public FunctionType getType() {
        return FunctionType.FASTDTW;
    }

    @Override
    public boolean needSubquery() {
        return true;
    }

    @Override
    public String getSubquery() {
        return subquery;
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
        FastDtw rhs = (FastDtw) obj;
        return new EqualsBuilder()
                .append(this.distanceFunction, rhs.distanceFunction)
                .append(this.searchRadius, rhs.searchRadius)
                .append(this.maxNormalizedWarpingCost, rhs.maxNormalizedWarpingCost)
                .append(this.subquery, rhs.subquery)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(distanceFunction)
                .append(searchRadius)
                .append(maxNormalizedWarpingCost)
                .append(subquery)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "FastDtw{" +
                "distanceFunction=" + distanceFunction +
                ", searchRadius=" + searchRadius +
                ", maxNormalizedWarpingCost=" + maxNormalizedWarpingCost +
                ", subquery='" + subquery + '\'' +
                '}';
    }
}
