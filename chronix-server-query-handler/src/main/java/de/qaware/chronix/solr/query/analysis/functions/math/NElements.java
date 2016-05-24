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
package de.qaware.chronix.solr.query.analysis.functions.math;

import java.util.Arrays;

/**
 * Class to calculate the top or bottom n values.
 *
 * @author f.lautenschlager
 */
public class NElements {

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
        int index;
        double value;

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
            if(value == o.value){
                return 0;
            }
            if(value > o.value){
                return 1;
            }
            return -1;
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
        NElementsResult(long[] nTimes, double[] nValues) {
            this.nTimes = nTimes;
            this.nValues = nValues;
        }

        /**
         * @return the n values
         */
        public double[] getNValues() {
            return nValues;
        }

        /**
         * @return the n times
         */
        public long[] getNTimes() {
            return nTimes;
        }
    }
}
