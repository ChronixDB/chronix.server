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

import static junit.framework.TestCase.assertSame;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * Basic pair unit test.
 */
public class PairTest {

    @Test
    public void testNormalUseScenario() {
        Pair<Integer, String> pair = new Pair<>(12, "Hallo");
        assertTrue(pair.toString().contains("12"));
        assertTrue(pair.toString().contains("Hallo"));

        Pair<Integer, String> pair2 = new Pair<>(12, "Hallo");
        Pair<Integer, String> pair3 = new Pair<>(13, "Hallo");
        Pair<Integer, String> pair4 = new Pair<>(12, "Hallo2");
        assertEquals(pair, pair2);
        assertFalse(pair.equals(pair3));
        assertFalse(pair.equals(pair4));

        assertEquals(pair.hashCode(), pair2.hashCode());

        assertSame(pair.getFirst(), 12);
        assertSame(pair.getSecond(), "Hallo");

        assertTrue(pair.asArray()[0].equals(12));
        assertTrue(pair.asArray()[1].equals("Hallo"));

        assertTrue(pair.asList().get(0).equals(12));
        assertTrue(pair.asList().get(1).equals("Hallo"));
    }

    @Test
    public void testNullHandling() {
        Pair<Integer, String> pair = new Pair<>(null, null);
        assertEquals(pair.toString(), "");
        assertNull(pair.getFirst());
        assertNull(pair.getSecond());
    }

}