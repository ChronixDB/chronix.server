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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Arrays;

/**
 * Class to calculate the top or bottom n values.
 *
 * @author f.lautenschlager
 */
public final class NElements {

    private NElements() {
        //avoid instances
    }

    /**
     * @param type        the calculation type: BOTTOM or TOP
     * @param n           the number of values n bottom or top values
     * @param timesStamps the time stamps of the time series
     * @param values      the belonging values of the time series
     * @return a result containing the top / bottom measurements (timestamp + value) of the time series
     */
    public static NElementsResult calc(NElementsCalculation type, int n, long[] timesStamps, double[] values) {
        //we have to convert them to a pair array, to not loose the reference to the time stamps
        Pair[] pairs = create(values);
        Arrays.sort(pairs);

        double[] nValues = new double[n];
        long[] nTimes = new long[n];


        switch (type) {
            case TOP:
                int j = 0;
                for (int i = timesStamps.length - 1; timesStamps.length - n <= i; i--) {
                    nValues[j] = pairs[i].value;
                    nTimes[j] = timesStamps[pairs[i].index];
                    j++;
                }
                break;
            case BOTTOM:
                for (int i = 0; i < n; i++) {
                    nValues[i] = pairs[i].value;
                    nTimes[i] = timesStamps[pairs[i].index];
                }
                break;
            default:
                throw new EnumConstantNotPresentException(NElementsCalculation.class, "Type: " + type + " not available");
        }

        return new NElementsResult(nTimes, nValues);
    }

    /**
     * @param values the values of the time series
     * @return a pair array of value and index
     */
    private static Pair[] create(double[] values) {
        Pair[] pairs = new Pair[values.length];
        for (int i = 0; i < values.length; i++) {
            pairs[i] = new Pair(i, values[i]);
        }
        return pairs;
    }

    /**
     * Helper class to represent values with belonging index
     */
    private static final class Pair implements Comparable<Pair> {
        private int index;
        private double value;

        /**
         * Constructs a pair
         *
         * @param index the index
         * @param value the value
         */
        Pair(int index, double value) {
            this.index = index;
            this.value = value;
        }

        @Override
        public int compareTo(Pair o) {
            if (value < o.value) {
                return -1;
            }
            if (value > o.value) {
                return 1;
            }
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o){
                return true;
            }

            if (o == null || getClass() != o.getClass()){
                return false;
            }

            Pair pair = (Pair) o;

            return new EqualsBuilder()
                    .append(index, pair.index)
                    .append(value, pair.value)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(index)
                    .append(value)
                    .toHashCode();
        }
    }

    /**
     * The two calculations
     */
    public enum NElementsCalculation {
        TOP,
        BOTTOM
    }

    /**
     * The N-Elements result
     */
    public static final class NElementsResult {
        private double[] nValues;
        private long[] nTimes;

        /**
         * @param nTimes  the n time stamps
         * @param nValues the n values
         */
        @SuppressWarnings("all")
        NElementsResult(long[] nTimes, double[] nValues) {
            this.nTimes = nTimes;
            this.nValues = nValues;
        }

        /**
         * @return the n values
         */
        @SuppressWarnings("all")
        public double[] getNValues() {
            return nValues;
        }

        /**
         * @return the n times
         */
        @SuppressWarnings("all")
        public long[] getNTimes() {
            return nTimes;
        }
    }
}
