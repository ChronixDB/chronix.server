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

import java.util.List;
import java.util.function.Function;

import static de.qaware.chronix.dts.Functions.compose;
import static de.qaware.chronix.dts.Functions.curryLeft;
import static de.qaware.chronix.dts.Functions.curryRight;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

/**
 * Basic Functions test.
 */
public class FunctionsTest {

    @Test
    public void testCompose() {
        Function<Pair<String, Integer>, Integer> f = Pair::getSecond;
        Function<Integer, Integer> g = x -> 2 * x;
        Function<Pair<String, Integer>, Integer> h = compose(g, f);
        Pair<String, Integer> p = new Pair<>("hello", 7);
        int s = h.apply(p);
        assertEquals(14, s);
    }

    @Test
    public void testCurry() {
        Function<List<Integer>, Integer> sum = FunctionsTest::sum;

        List<Integer> args = asList(1, 2, 3, 4, 5, null);

        Function<List<Integer>, Integer> sum100 = curryLeft(sum, asList(100));
        Integer s = sum100.apply(args);
        assertEquals(115, s.intValue());

        Function<List<Integer>, Integer> sum200 = curryRight(sum, asList(200));
        s = sum200.apply(args);
        assertEquals(215, s.intValue());

        sum200 = curryRight(sum, asList(0));
        s = sum200.apply(args);
        assertEquals(15, s.intValue());

        Function<Integer, Integer> add7 = curryLeft((x, y) -> x + y, 7);
        assertEquals(Integer.valueOf(15), add7.apply(8));

        Function<Integer, Integer> add9 = curryRight((x, y) -> x + y, 9);
        assertEquals(Integer.valueOf(17), add9.apply(8));
    }

    private static Integer sum(List<Integer> ints) {
        Integer sum = Integer.valueOf(0);
        for (Integer i : ints) {
            if (i != null) {
                sum = sum + i;
            }
        }
        return sum;
    }
}






