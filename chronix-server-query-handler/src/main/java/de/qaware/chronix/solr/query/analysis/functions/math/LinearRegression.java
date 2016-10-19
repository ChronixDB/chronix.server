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
import de.qaware.chronix.converter.common.LongList;

/**
 * Simple linear regression to calculate the slope
 *
 * @author f.lautenschlager
 */
public class LinearRegression {
    private final double intercept;

    /**
     * Performs a linear regression on the data points
     *
     * @throws IllegalArgumentException if the lengths of the two arrays are not equal
     */
    public LinearRegression(LongList timestamps, DoubleList values) {
        double[] x = new double[timestamps.size()];
        double[] y = new double[values.size()];

        for (int i = 0; i < timestamps.size(); i++) {
            x[i] = timestamps.get(i);
            y[i] = values.get(i);
        }

        double sumX = 0.0;
        double sumY = 0.0;
        for (int i = 0; i < x.length; i++) {
            sumX += x[i];
            sumY += y[i];
        }
        double xBar = sumX / x.length;
        double yBar = sumY / x.length;

        double xxBar = 0.0;
        double xyBar = 0.0;
        for (int i = 0; i < x.length; i++) {
            xxBar += (x[i] - xBar) * (x[i] - xBar);
            xyBar += (x[i] - xBar) * (y[i] - yBar);
        }
        intercept = xyBar / xxBar;
    }


    /**
     * Returns the slope &intercept; of the best of the best-fit line <em>y</em> = &slope; + &intercept; <em>x</em>.
     *
     * @return the slope &intercept; of the best-fit line <em>y</em> = &slope; + &intercept; <em>x</em>
     */
    public double slope() {
        return intercept;
    }
}