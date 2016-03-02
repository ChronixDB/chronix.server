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


import de.qaware.chronix.timeseries.dt.DoubleList;

/**
 * Class to calculate the standard deviation
 *
 * @author f.lautenschlager
 */
public final class StdDev {

    private StdDev() {
    }

    /**
     * Calculates the standard deviation
     *
     * @param doubles a list with values
     * @return the standard deviation
     */
    public static double dev(DoubleList doubles) {
        if (doubles.isEmpty()) {
            return Double.NaN;
        }

        return Math.sqrt(variance(doubles));
    }

    private static double mean(DoubleList values) {
        double sum = 0.0;
        for (int i = 0; i < values.size(); i++) {
            sum = sum + values.get(i);
        }

        return sum / values.size();
    }

    private static double variance(DoubleList doubles) {
        double avg = mean(doubles);
        double sum = 0.0;
        for (int i = 0; i < doubles.size(); i++) {
            double value = doubles.get(i);
            sum += (value - avg) * (value - avg);
        }
        return sum / (doubles.size() - 1);
    }


}
