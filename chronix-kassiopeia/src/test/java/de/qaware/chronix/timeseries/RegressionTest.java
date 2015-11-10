/*
 * #%L
 * QAcommons - The QAware Standard Library - Time Series
 * %%
 * Copyright (C) 2014 - 2015 QAware GmbH
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package de.qaware.chronix.timeseries;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.junit.Test;

/**
 * Basic unit test for a regression.
 */
public class RegressionTest {

    @Test
    public void testRegression() {

        SimpleRegression regression = new SimpleRegression();
        regression.addData(0.0, 1.0);
        regression.addData(1.0, 2.5);
        regression.addData(2.0, 3.0);

        double slope = regression.getSlope();
        double intercept = regression.getIntercept();
        long n = regression.getN();
        double err = regression.getMeanSquareError();
    }
}
