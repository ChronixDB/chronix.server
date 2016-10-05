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

/**
 * Derivative math util
 *
 * @author f.lautenschlager
 */
public final class DerivativeUtil {

    private DerivativeUtil() {
        //avoid instances
    }

    /**
     * Calculates derivative of two points of a time series.
     * It uses the following algorithm:
     * <pre>
     *                  xT2 - xT0
     * derivative = ---------------
     *              2 * (yT2 - yT0)
     * </pre>
     * The algorithm uses a centered difference.
     *
     * @param xT2 the next value
     * @param xT0 the current value
     * @param yT2 the next timestamp
     * @param yT0 the current timestamp
     * @return the derivative value
     */
    public static double derivative(double xT2, double xT0, long yT2, long yT0) {
        //convert the given timestamps into seconds
        long deltaTinSeconds = (yT2 - yT0) / 1000;
        //calculate the derivative
        return (xT2 - xT0) / (2 * deltaTinSeconds);
    }


}
