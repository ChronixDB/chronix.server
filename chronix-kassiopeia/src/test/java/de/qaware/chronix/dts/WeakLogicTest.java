/*
 * #%L
 * QAcommons - The QAware Standard Library - Data Types
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
package de.qaware.chronix.dts;


import org.junit.Test;

import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static de.qaware.chronix.dts.WeakLogic.*;
import static junit.framework.TestCase.assertSame;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;


/**
 * Basic WeakLogic test.
 */
public class WeakLogicTest {

    @Test
    public void testWeakComparator() {
        Comparator<Integer> cmp = (x, y) -> x - y;

        Comparator<Integer> w = weakComparator(cmp);

        assertTrue(w.compare(0, 1) < 0);
        assertTrue(w.compare(1, 0) > 0);
        assertSame(w.compare(1, 1), 0);
        assertTrue(w.compare(null, 1) < 0);
        assertTrue(w.compare(1, null) > 0);
        assertSame(w.compare(null, null), 0);

        w = weakComparator();

        assertTrue(w.compare(0, 1) < 0);
        assertTrue(w.compare(1, 0) > 0);
        assertSame(w.compare(1, 1), 0);
        assertTrue(w.compare(null, 1) < 0);
        assertTrue(w.compare(1, null) > 0);
        assertSame(w.compare(null, null), 0);

        cmp = weakComparator();

        int result;

        result = cmp.compare(null, null);
        assertEquals(result, 0);
        result = cmp.compare(null, 1);
        assertEquals(result, -1);
        result = cmp.compare(1, 2);
        assertEquals(result, -1);
        result = cmp.compare(1, null);
        assertEquals(result, 1);
    }


    @Test
    public void testWeakBinaryOperator() {
        BinaryOperator<Integer> weakAdd = weakBinaryOperator((Integer a, Integer b) -> a + b);
        Integer sum;

        sum = weakAdd.apply(null, null);
        assertNull(sum);

        sum = weakAdd.apply(7, null);
        assertEquals(Integer.valueOf(7), sum);

        sum = weakAdd.apply(null, 8);
        assertEquals(Integer.valueOf(8), sum);

        sum = weakAdd.apply(7, 8);
        assertEquals(Integer.valueOf(15), sum);
    }

    @Test
    public void testWeakUnaryOperator() {
        UnaryOperator<Integer> weak2 = weakUnaryOperator(x -> 2 * x);
        int s = weak2.apply(5);
        assertEquals(10, s);
        assertNull(weak2.apply(null));
    }

    @Test
    public void testWeakFunction() {
        Function<Integer, Integer> weak2 = weakFunction(x -> 2 * x);
        int s = weak2.apply(5);
        assertEquals(10, s);
        assertNull(weak2.apply(null));
    }

    @Test
    public void testWeakEquals() {
        boolean b = weakEquals(null, null);
        assertTrue(b);

        b = weakEquals("xxx", null);
        assertFalse(b);

        b = weakEquals(null, "xxx");
        assertFalse(b);

        b = weakEquals("xxx", "xxx");
        assertTrue(b);
    }
}






