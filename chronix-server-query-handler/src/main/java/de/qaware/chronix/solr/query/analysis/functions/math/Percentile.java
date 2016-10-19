/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis.functions.math;


import de.qaware.chronix.converter.common.DoubleList;

import java.util.Arrays;

/**
 * Class to calculate a percentile
 *
 * @author f.lautenschlager
 */
public final class Percentile {

    /**
     * Avoid instances
     */
    private Percentile() {
    }

    /**
     * Implemented the quantile type 7 referred to
     * http://tolstoy.newcastle.edu.au/R/e17/help/att-1067/Quartiles_in_R.pdf
     * and
     * http://stat.ethz.ch/R-manual/R-patched/library/stats/html/quantile.html
     * as its the default quantile implementation
     * <p>
     * <code>
     * QuantileType7 = function (v, p) {
     * v = sort(v)
     * h = ((length(v)-1)*p)+1
     * v[floor(h)]+((h-floor(h))*(v[floor(h)+1]- v[floor(h)]))
     * }
     * </code>
     *
     * @param values     - the values to aggregate the percentile
     * @param percentile - the percentile (0 - 1), e.g. 0.25
     * @return the value of the n-th percentile
     */
    public static double evaluate(DoubleList values, double percentile) {
        double[] doubles = values.toArray();
        Arrays.sort(doubles);

        return evaluateForDoubles(doubles, percentile);
    }

    private static double evaluateForDoubles(double[] points, double percentile) {
        //For example:
        //values    = [1,2,2,3,3,3,4,5,6], size = 9, percentile (e.g. 0.25)
        // size - 1 = 8 * 0.25 = 2 (~ 25% from 9) + 1 = 3 => values[3] => 2
        double percentileIndex = ((points.length - 1) * percentile) + 1;

        double rawMedian = points[floor(percentileIndex - 1)];
        double weight = percentileIndex - floor(percentileIndex);

        if (weight > 0) {
            double pointDistance = points[floor(percentileIndex - 1) + 1] - points[floor(percentileIndex - 1)];
            return rawMedian + weight * pointDistance;
        } else {
            return rawMedian;
        }
    }

    /**
     * Wraps the Math.floor function and casts it to an integer
     *
     * @param value - the evaluatedValue
     * @return the floored evaluatedValue
     */
    private static int floor(double value) {
        return (int) Math.floor(value);
    }


}
