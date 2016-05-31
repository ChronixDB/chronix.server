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
