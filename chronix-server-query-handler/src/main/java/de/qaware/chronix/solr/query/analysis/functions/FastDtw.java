/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis.functions;

import de.qaware.chronix.distance.DistanceFunction;
import de.qaware.chronix.distance.DistanceFunctionEnum;
import de.qaware.chronix.distance.DistanceFunctionFactory;
import de.qaware.chronix.dtw.FastDTW;
import de.qaware.chronix.dtw.TimeWarpInfo;
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
    public double execute(MetricTimeSeries... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Fast DTW needs at least two time series");
        }

        MultivariateTimeSeries origin = buildMultiVariateTimeSeries(args[0]);
        MultivariateTimeSeries other = buildMultiVariateTimeSeries(args[1]);
        TimeWarpInfo result = FastDTW.getWarpInfoBetween(origin, other, searchRadius, distanceFunction);
        if (result.getNormalizedDistance() <= maxNormalizedWarpingCost) {
            return result.getNormalizedDistance();
        }
        return -1;
    }

    /**
     * Builds a multivariate time series of the given univariate time series
     *
     * @param timeSeries the metric time series
     * @return a multivariate time series for the fast dtw analysis
     */
    private MultivariateTimeSeries buildMultiVariateTimeSeries(MetricTimeSeries timeSeries) {
        MultivariateTimeSeries multivariateTimeSeries = new MultivariateTimeSeries(1);
        timeSeries.sort();
        timeSeries.points().forEachOrdered(pair -> {
            multivariateTimeSeries.add(pair.getTimestamp(), new double[]{pair.getValue()});
        });
        return multivariateTimeSeries;
    }

    @Override
    public String[] getArguments() {
        return new String[]{"search radius=" + searchRadius,
                "max warping cost=" + maxNormalizedWarpingCost,
                "distance function=" + DistanceFunctionEnum.EUCLIDEAN.name()};
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
