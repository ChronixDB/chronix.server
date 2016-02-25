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
