/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis.functions.analyses;

import de.qaware.chronix.distance.DistanceFunction;
import de.qaware.chronix.distance.DistanceFunctionEnum;
import de.qaware.chronix.distance.DistanceFunctionFactory;
import de.qaware.chronix.dtw.FastDTW;
import de.qaware.chronix.dtw.TimeWarpInfo;
import de.qaware.chronix.solr.query.analysis.FunctionValueMap;
import de.qaware.chronix.solr.query.analysis.functions.ChronixPairAnalysis;
import de.qaware.chronix.solr.query.analysis.functions.FunctionType;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import de.qaware.chronix.timeseries.MultivariateTimeSeries;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.solr.common.util.Pair;

/**
 * The analysis implementation of the Fast DTW analysis
 *
 * @author f.lautenschlager
 */
public final class FastDtw implements ChronixPairAnalysis<Pair<MetricTimeSeries, MetricTimeSeries>> {

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
    public void execute(Pair<MetricTimeSeries, MetricTimeSeries> timeSeriesPair, FunctionValueMap functionValueMap) {
        //We have to build a multivariate time series
        MultivariateTimeSeries origin = buildMultiVariateTimeSeries(timeSeriesPair.first());
        MultivariateTimeSeries other = buildMultiVariateTimeSeries(timeSeriesPair.second());
        //Call the fast dtw library
        TimeWarpInfo result = FastDTW.getWarpInfoBetween(origin, other, searchRadius, distanceFunction);
        //Check the result. If it lower equals the threshold, we can return the other time series
        //TODO: Add the result to the time series

        functionValueMap.add(this, result.getNormalizedDistance() <= maxNormalizedWarpingCost, timeSeriesPair.second().getMetric());

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
