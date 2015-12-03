/*
 * Copyright (C) 2015 QAware GmbH
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
package de.qaware.chronix.solr.query.analysis.collectors.math;

import java.util.List;

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
    public static double dev(List<Double> doubles) {
        if (doubles.isEmpty()) {
            return Double.NaN;
        }

        return Math.sqrt(variance(doubles));
    }

    private static double mean(List<Double> a) {
        double sum = 0.0;
        for (Double anA : a) {
            sum = sum + anA;
        }
        return sum / a.size();
    }

    private static double variance(List<Double> doubles) {
        double avg = mean(doubles);
        double sum = 0.0;
        for (Double anA : doubles) {
            sum += (anA - avg) * (anA - avg);
        }
        return sum / (doubles.size() - 1);
    }


}
