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
import de.qaware.chronix.dts.WeakLogic;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

import static de.qaware.chronix.dts.Pair.pairOf;
import static de.qaware.chronix.dts.WeakLogic.weakComparator;
import static de.qaware.chronix.iterators.Iterators.of;
import static de.qaware.chronix.iterators.Iterators.takeAll;
import static de.qaware.chronix.timeseries.TimeSeries.merge;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * Basic unit test for the time series.
 */
public class TimeSeriesTest {

    @Test
    public void testApply() {
        List<Pair<Integer, Integer>> ts = new ArrayList<>();
        ts.add(pairOf(null, null));
        ts.add(pairOf(null, null));
        ts.add(pairOf(null, null));
        ts.add(pairOf(null, null));
        ts.add(pairOf(0, 9));
        ts.add(pairOf(0, 8));
        ts.add(pairOf(0, 7));
        ts.add(pairOf(10, 17));
        ts.add(pairOf(20, 27));

        TimeSeries<Integer, Integer> sf = new TimeSeries<>(ts);
        assertEquals(sf.size(), 4);
        assertEquals(sf.apply(null), null);
        assertEquals(sf.apply(-1), null);
        assertEquals(sf.apply(0), Integer.valueOf(7));
        assertEquals(sf.apply(1), Integer.valueOf(7));
        assertEquals(sf.apply(10), Integer.valueOf(17));
        assertEquals(sf.apply(11), Integer.valueOf(17));
        assertEquals(sf.apply(20), Integer.valueOf(27));
        assertEquals(sf.apply(10000), Integer.valueOf(27));

        ts = new ArrayList<>();
        ts.add(pairOf(null, -7));
        ts.add(pairOf(0, 7));
        ts.add(pairOf(10, 17));
        ts.add(pairOf(20, null));

        sf = new TimeSeries<>(ts);
        assertEquals(sf.apply(-10000), Integer.valueOf(-7));
        assertNull(sf.apply(20));
        assertNull(sf.apply(10000));

        ts = new ArrayList<>();
        ts.add(pairOf(null, -7));
        ts.add(pairOf(0, 70));
        ts.add(pairOf(10, 170));    // ignore
        ts.add(pairOf(10, 180));    // ignore
        ts.add(pairOf(10, 190));    // ignore
        ts.add(pairOf(10, 200));
        ts.add(pairOf(20, 200));    // ignore
        ts.add(pairOf(30, 200));    // ignore
        ts.add(pairOf(40, null));
        ts.add(pairOf(50, null));   // ignore
        ts.add(pairOf(60, null));   // ignore

        sf = new TimeSeries<>(ts);
        assertEquals(sf.apply(-10000), Integer.valueOf(-7));
        assertEquals(sf.apply(0), Integer.valueOf(70));
        assertEquals(sf.apply(10), Integer.valueOf(200));
        assertEquals(sf.apply(30), Integer.valueOf(200));
        assertEquals(sf.apply(35), Integer.valueOf(200));
        assertNull(sf.apply(40));
        assertNull(sf.apply(40000));

        ts = takeAll(sf.iterator());
        assertEquals(ts.size(), 4);
    }

    @Test
    public void testMerge1() {
        Iterator<Pair<Integer, Integer>> ts = of(
                pairOf((Integer) null, (Integer) null),
                pairOf(0, 31),
                pairOf(10, 41),
                pairOf(20, (Integer) null));
        TimeSeries<Integer, Integer> t = new TimeSeries<>(ts);

        ts = of(
                pairOf((Integer) null, (Integer) null),
                pairOf(0, 63),
                pairOf(5, 23),
                pairOf(15, 53),
                pairOf(20, (Integer) null));
        TimeSeries<Integer, Integer> r = new TimeSeries<>(ts);

        TimeSeries<Integer, Integer> sh = merge(asList(r, t), (a, b) -> a + b);

        assertNull(sh.apply(-1));
        assertEquals(sh.apply(0), Integer.valueOf(94));
        assertEquals(sh.apply(0), new Integer(94));
        assertEquals(sh.apply(1), new Integer(94));
        assertEquals(sh.apply(5), new Integer(54));
        assertEquals(sh.apply(10), new Integer(64));
        assertEquals(sh.apply(12), new Integer(64));
        assertEquals(sh.apply(15), new Integer(94));
        assertEquals(sh.apply(18), new Integer(94));
        assertEquals(sh.apply(20), null);
        assertEquals(sh.apply(25), null);
        assertEquals(sh.apply(2000), null);
    }


    @Test
    public void testMerge2() {

        Iterator<Pair<Integer, Integer>> ts = of(
                pairOf((Integer) null, (Integer) null),
                pairOf(0, 200),
                pairOf(10, 100),
                pairOf(20, 200),
                pairOf(30, (Integer) null));
        TimeSeries<Integer, Integer> t = new TimeSeries<>(ts);

        Iterator<Pair<Integer, Integer>> rs = of(
                pairOf((Integer) null, (Integer) null),
                pairOf(0, 100),
                pairOf(10, 200),
                pairOf(20, 100),
                pairOf(30, (Integer) null));
        TimeSeries<Integer, Integer> r = new TimeSeries<>(rs);

        TimeSeries<Integer, Integer> sh = merge(asList(r, t), (a, b) -> a + b);

        assertNull(sh.apply(-1));
        assertEquals(sh.apply(0), new Integer(300));
        assertEquals(sh.apply(10), new Integer(300));
        assertEquals(sh.apply(20), new Integer(300));
        assertEquals(sh.apply(25), new Integer(300));
        assertEquals(sh.apply(30), null);
        assertEquals(sh.apply(35), null);

        ts = of(pairOf((Integer) null, 500));
        Iterator<Pair<Integer, Integer>> rr = of(
                pairOf((Integer) null, (Integer) null),
                pairOf(0, 700));
        t = new TimeSeries<>(ts);
        r = new TimeSeries<>(rr);

        sh = merge(asList(r, t), (a, b) -> a + b);

        assertEquals(sh.apply(-1000), new Integer(500));
        assertEquals(sh.apply(-100), new Integer(500));
        assertEquals(sh.apply(0), new Integer(1200));
        assertEquals(sh.apply(100), new Integer(1200));
        assertEquals(sh.apply(1000), new Integer(1200));
    }


    @Test
    public void testMerge3() {

        Iterator<Pair<Integer, Integer>> ts = of(pairOf((Integer) null, 500));
        Iterator<Pair<Integer, Integer>> rr = of(
                pairOf((Integer) null, (Integer) null),
                pairOf(0, 700));
        TimeSeries<Integer, Integer> t = new TimeSeries<>(ts);
        TimeSeries<Integer, Integer> r = new TimeSeries<>(rr);

        TimeSeries<Integer, Integer> sh = merge(asList(r, t), (a, b) -> a + b);

        assertEquals(sh.apply(-1000), new Integer(500));
        assertEquals(sh.apply(-100), new Integer(500));
        assertEquals(sh.apply(0), new Integer(1200));
        assertEquals(sh.apply(100), new Integer(1200));
        assertEquals(sh.apply(1000), new Integer(1200));
    }

    @Test
    public void testMerge4() {
        Pair<Integer, Boolean> p = pairOf(null, false);
        TimeSeries<Integer, Boolean> as = new TimeSeries<>(of(p));
        Pair<Integer, Boolean> q = pairOf(null, true);
        TimeSeries<Integer, Boolean> bs = new TimeSeries<>(of(q));
        TimeSeries<Integer, Boolean> result = merge(as, bs, (x, y) -> !x || y);
        assertEquals(1, result.size());
        assertTrue(result.apply(0));

        Iterator<Pair<Integer, Integer>> ts = of(
                pairOf((Integer) null, (Integer) null),
                pairOf(0, 7),
                pairOf(10, 17),
                pairOf(20, 27));
        TimeSeries<Integer, Integer> sv = new TimeSeries<>(ts);

        Iterator<Pair<Integer, Integer>> rs = of(
                pairOf((Integer) null, (Integer) null),
                pairOf(5, 6),
                pairOf(15, 12),
                pairOf(22, 24));
        TimeSeries<Integer, Integer> tv = new TimeSeries<>(rs);

        Comparator<Integer> c = weakComparator();

        BiFunction<Integer, Integer, Boolean> cmp = (x, y) -> c.compare(x, y) >= 0;
        result = merge(sv, tv, cmp);
        assertEquals(1, result.size());
        assertTrue(result.apply(0));
    }


    @Test
    public void testSubSeries() {

        List<Pair<Integer, Integer>> ts = new ArrayList<>();
        ts.add(pairOf(0, 7));
        ts.add(pairOf(10, 17));
        ts.add(pairOf(20, 27));
        TimeSeries<Integer, Integer> tv = new TimeSeries<>(ts.iterator());

        try {
            tv.subSeries(0, 0);
            fail();
        } catch (IllegalArgumentException e) {

        }

        assertEquals(tv.subSeries(null, 1000).size(), 5);
        assertEquals(tv.subSeries(null, -10).size(), 1);
        assertEquals(tv.subSeries(null, 0).size(), 1);
        assertEquals(tv.subSeries(null, 7).size(), 3);
        assertEquals(tv.subSeries(null, 10).size(), 3);
        assertEquals(tv.subSeries(null, 17).size(), 4);
        assertEquals(tv.subSeries(null, 20).size(), 4);
        assertEquals(tv.subSeries(null, 27).size(), 5);
        assertEquals(tv.subSeries(null, 100).size(), 5);
        assertEquals(tv.subSeries(-10, 1000).size(), 5);
        assertEquals(tv.subSeries(0, 1000).size(), 5);
        assertEquals(tv.subSeries(5, 1000).size(), 5);
        assertEquals(tv.subSeries(10, 1000).size(), 4);
        assertEquals(tv.subSeries(15, 1000).size(), 4);
        assertEquals(tv.subSeries(20, 1000).size(), 3);
        assertEquals(tv.subSeries(25, 1000).size(), 3);
        assertEquals(tv.subSeries(null, -100).size(), 1);
        assertEquals(tv.subSeries(0, 1).size(), 3);
    }


    @Test
    public void testSpecials() {
        Pair<Integer, Boolean> p = pairOf(null, false);
        Pair<Integer, Boolean> q = pairOf(0, true);
        Pair<Integer, Boolean> r = pairOf(10, true);

        Iterator<Iterator<Pair<Integer, Boolean>>> xx =
                of(asList(p, q).iterator(), asList(p, r).iterator());
        Iterator<Pair<Integer, List<Boolean>>> tt = new TimeSeriesMerge<>(xx);
        TimeSeries<Integer, List<Boolean>> vv = new TimeSeries<>(tt);
        assertEquals(3, vv.size());
    }

    @Test
    public void testConstructors() {
        Iterator<Pair<Integer, Integer>> pt = of(
                pairOf((Integer) null, (Integer) null),
                pairOf(5, 6),
                pairOf(15, 12),
                pairOf(22, 24));
        TimeSeries<Integer, Integer> tv = new TimeSeries<>(pt);
        TimeSeries<Integer, Integer> tw = new TimeSeries<>(tv);

        assertEquals(tv, tv);
        assertEquals(tv, tw);
        assertEquals(tw, tw);

        assertEquals(tv.hashCode(), tw.hashCode());

        BiFunction<Integer, Integer, Boolean> f = WeakLogic::weakEquals;
        TimeSeries<Integer, Boolean> tb = merge(tv, tw, f);
        assertEquals(tb.size(), 1);
        assertTrue(tb.apply(null));
    }

    @Test
    public void testRelocate() {
        Iterator<Pair<Integer, Integer>> ps = of(
                pairOf((Integer) null, (Integer) null),
                pairOf(50, 6),
                pairOf(150, 12),
                pairOf(220, 24));
        TimeSeries<Integer, Integer> tv = new TimeSeries<>(ps);

        TimeSeries<Integer, Integer> qs = tv.relocate(of(60, 160));
        assertSame(qs.apply(50), null);
        assertSame(qs.apply(60), 6);
        assertSame(qs.apply(150), 6);
        assertSame(qs.apply(160), 12);
        assertSame(qs.apply(10000), 12);
    }


    @Test
    public void testSameLeg() {
        Iterator<Pair<Integer, Integer>> ps = of(
                pairOf((Integer) null, (Integer) null),
                pairOf(50, 6),
                pairOf(150, 12),
                pairOf(220, 24));
        TimeSeries<Integer, Integer> tv = new TimeSeries<>(ps);

        assertTrue(tv.sameLeg(null, 0));
        assertTrue(tv.sameLeg(0, 49));
        assertTrue(tv.sameLeg(0, 50));
        assertFalse(tv.sameLeg(0, 51));
        assertTrue(tv.sameLeg(50, 150));
        assertFalse(tv.sameLeg(0, 150));
        assertTrue(tv.sameLeg(220, 10000));
    }
}