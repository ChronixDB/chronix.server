/*
 * #%L
 * QAcommons - The QAware Standard Library - Time Series
 * %%
 * Copyright (C) 2014 QAware GmbH
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


import de.qaware.chronix.dts.Pair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static de.qaware.chronix.iterators.Iterators.empty;
import static de.qaware.chronix.iterators.Iterators.takeAll;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Basic unit test for the TimeSeriesRegression.
 */
public class TimeSeriesRegressionTest {


    @Test
    public void testNoValue() {

        double epsilon = 0.1;

        // call linearize
        Iterator<Pair<Double, Pair<Double, Double>>> result = TimeSeriesUtil.linearize(empty(), epsilon);

        // unpack result
        List<Pair<Double, Pair<Double, Double>>> resultAsList = takeAll(result);
        assertThat(resultAsList.size(), is(0));
    }


    @Test
    public void testSingleValue() {

        List<Pair<Double, Double>> ts = new ArrayList<>();
        ts.add(Pair.pairOf(0.0, 100.0));

        double epsilon = 0.1;

        // call linearize
        Iterator<Pair<Double, Pair<Double, Double>>> result = TimeSeriesUtil.linearize(ts.iterator(), epsilon);

        // unpack result
        List<Pair<Double, Pair<Double, Double>>> resultAsList = takeAll(result);

        assertThat(resultAsList.size(), is(1));
        Pair<Double, Pair<Double, Double>> p = resultAsList.get(0);
        assertThat(p.getFirst(), is(0.0));
        assertThat(p.getSecond(), is(Pair.pairOf(100.0, 0.0)));
    }


    @Test
    public void testSingleHorizontalSegment() {

        List<Pair<Double, Double>> ts = new ArrayList<>();
        ts.add(Pair.pairOf(0.0, 100.0));
        ts.add(Pair.pairOf(1.0, 100.0));
        ts.add(Pair.pairOf(3.0, 100.0));
        ts.add(Pair.pairOf(7.0, 100.0));
        ts.add(Pair.pairOf(9.0, 100.0));
        ts.add(Pair.pairOf(15.0, 100.0));
        ts.add(Pair.pairOf(30.0, 100.0));
        double epsilon = 0.1;

        // call linearize
        Iterator<Pair<Double, Pair<Double, Double>>> result = TimeSeriesUtil.linearize(ts.iterator(), epsilon);

        // unpack result
        List<Pair<Double, Pair<Double, Double>>> resultAsList = takeAll(result);

        assertThat(resultAsList.size(), is(1));
        Pair<Double, Pair<Double, Double>> p = resultAsList.get(0);
        assertThat(p.getFirst(), is(0.0));
        assertThat(p.getSecond(), is(Pair.pairOf(100.0, 0.0)));
    }


    @Test
    public void testManyHorizontalSegments() {
        List<Pair<Double, Double>> ts = new ArrayList<>();
        ts.add(Pair.pairOf(0.0, 100.0));
        ts.add(Pair.pairOf(1.0, 100.0));
        ts.add(Pair.pairOf(3.0, 100.0));
        ts.add(Pair.pairOf(7.0, 100.0));
        ts.add(Pair.pairOf(9.0, 100.0));
        ts.add(Pair.pairOf(15.0, 100.0));
        ts.add(Pair.pairOf(30.0, 100.0));

        ts.add(Pair.pairOf(40.0, 200.0));
        ts.add(Pair.pairOf(43.0, 200.0));
        ts.add(Pair.pairOf(45.0, 200.0));
        ts.add(Pair.pairOf(48.0, 200.0));
        ts.add(Pair.pairOf(51.0, 200.0));
        ts.add(Pair.pairOf(55.0, 200.0));
        ts.add(Pair.pairOf(59.0, 200.0));

        ts.add(Pair.pairOf(70.0, 300.0));
        ts.add(Pair.pairOf(71.0, 300.0));
        ts.add(Pair.pairOf(73.0, 300.0));
        ts.add(Pair.pairOf(77.0, 300.0));
        ts.add(Pair.pairOf(91.0, 300.0));
        ts.add(Pair.pairOf(95.0, 300.0));
        ts.add(Pair.pairOf(96.0, 300.0));

        double epsilon = 0.1;

        // call linearize
        Iterator<Pair<Double, Pair<Double, Double>>> result = TimeSeriesUtil.linearize(ts.iterator(), epsilon);

        // unpack result
        List<Pair<Double, Pair<Double, Double>>> resultAsList = takeAll(result);

        assertThat(resultAsList.size(), is(3));
        Pair<Double, Pair<Double, Double>> p = resultAsList.get(0);
        assertThat(p.getFirst(), is(0.0));
        assertThat(p.getSecond(), is(Pair.pairOf(100.0, 0.0)));

        p = resultAsList.get(1);
        assertThat(p.getFirst(), is(40.0));
        assertThat(p.getSecond(), is(Pair.pairOf(200.0, 0.0)));

        p = resultAsList.get(2);
        assertThat(p.getFirst(), is(70.0));
        assertThat(p.getSecond(), is(Pair.pairOf(300.0, 0.0)));
    }


    @Test
    public void testSingleInclinedSegment() {

        List<Pair<Double, Double>> ts = new ArrayList<>();

        ts.add(Pair.pairOf(1.0, 5.0));
        ts.add(Pair.pairOf(2.0, 10.0));
        ts.add(Pair.pairOf(3.0, 15.0));

        double epsilon = 0.1;

        // call linearize
        Iterator<Pair<Double, Pair<Double, Double>>> result = TimeSeriesUtil.linearize(ts.iterator(), epsilon);

        // unpack result
        List<Pair<Double, Pair<Double, Double>>> resultAsList = takeAll(result);

        assertThat(resultAsList.size(), is(1));
        Pair<Double, Pair<Double, Double>> p = resultAsList.get(0);
        assertThat(p.getFirst(), is(1.0));
        assertThat(p.getSecond(), is(Pair.pairOf(5.0, 5.0)));
    }


    @Test
    public void testManyInclinedSegments() {

        List<Pair<Double, Double>> ts = new ArrayList<>();

        ts.add(Pair.pairOf(0.0, 20.0));    // first segment, slope = 2.0
        ts.add(Pair.pairOf(1.0, 22.0));
        ts.add(Pair.pairOf(2.0, 24.0));
        ts.add(Pair.pairOf(3.0, 26.0));

        ts.add(Pair.pairOf(4.0, 23.0));    // second segment, slope = -3.0
        ts.add(Pair.pairOf(5.0, 20.0));
        ts.add(Pair.pairOf(6.0, 17.0));
        ts.add(Pair.pairOf(7.0, 14.0));

        ts.add(Pair.pairOf(8.0, 18.0));    // third segment, slope = 4.0
        ts.add(Pair.pairOf(9.0, 22.0));
        ts.add(Pair.pairOf(10.0, 26.0));
        ts.add(Pair.pairOf(11.0, 30.0));

        double epsilon = 0.1;

        // call linearize
        Iterator<Pair<Double, Pair<Double, Double>>> result = TimeSeriesUtil.linearize(ts.iterator(), epsilon);

        // unpack result
        List<Pair<Double, Pair<Double, Double>>> resultAsList = takeAll(result);

        assertThat(resultAsList.size(), is(3));
        Pair<Double, Pair<Double, Double>> p = resultAsList.get(0);
        assertThat(p.getFirst(), is(0.0));
        assertThat(p.getSecond(), is(Pair.pairOf(20.0, 2.0)));

        p = resultAsList.get(1);
        assertThat(p.getFirst(), is(4.0));
        assertThat(p.getSecond(), is(Pair.pairOf(23.0, -3.0)));

        p = resultAsList.get(2);
        assertThat(p.getFirst(), is(8.0));
        assertThat(p.getSecond(), is(Pair.pairOf(18.0, 4.0)));
    }


    @Test
    public void testTwoSimilarSegments() {

        List<Pair<Double, Double>> ts = new ArrayList<>();

        double epsilon = 1e-3;
        double slope = 1.10;

        ts.add(Pair.pairOf(0.0, 0.0));              // first segment, slope = 1.0
        ts.add(Pair.pairOf(1.0, 1.0));
        ts.add(Pair.pairOf(2.0, 2.0));
        ts.add(Pair.pairOf(3.0, 3.0));

        ts.add(Pair.pairOf(4.0, 3.0 + 1 * slope));    // second segment, slope = slope
        ts.add(Pair.pairOf(5.0, 3.0 + 2 * slope));
        ts.add(Pair.pairOf(6.0, 3.0 + 3 * slope));
        ts.add(Pair.pairOf(7.0, 3.0 + 4 * slope));

        // call linearize
        Iterator<Pair<Double, Pair<Double, Double>>> result = TimeSeriesUtil.linearize(ts.iterator(), epsilon);

        // unpack result
        List<Pair<Double, Pair<Double, Double>>> resultAsList = takeAll(result);

        assertThat(resultAsList.size(), is(2));
        Pair<Double, Pair<Double, Double>> p = resultAsList.get(0);
        assertThat(p.getFirst(), is(0.0));
        assertThat(p.getSecond(), is(Pair.pairOf(0.0, 1.0)));

        p = resultAsList.get(1);
        assertThat(p.getFirst(), is(4.0));
        assertThat(p.getSecond(), is(Pair.pairOf(3.0 + slope, slope)));
    }

    @Test
    public void testPerformance() {

        double epsilon = 1e-3;
        double m = 1e3;
        double delta = 1e-3;  // generates m/delta timestamps

        List<Pair<Double, Double>> ts = new ArrayList<>();

        for (int cnt = 0; cnt < m; cnt++) {                // for all segments
            double slope = Math.random();
            for (double x = 0.0; x < 1.0; x += delta) {    // proceed by delta
                ts.add(Pair.pairOf(cnt + x, cnt + x * slope));
            }
        }

        // call linearize
        Iterator<Pair<Double, Pair<Double, Double>>> result = TimeSeriesUtil.linearize(ts.iterator(), epsilon);

        // unpack result
        List<Pair<Double, Pair<Double, Double>>> resultAsList = takeAll(result);
        assertThat(resultAsList.size() > 0, is(true));
    }
}
