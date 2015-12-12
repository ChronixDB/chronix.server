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
    public LinearRegression(List<Long> timestamps, List<Double> values) {
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