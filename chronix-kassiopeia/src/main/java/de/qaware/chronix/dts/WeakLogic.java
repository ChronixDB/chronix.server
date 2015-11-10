/*
 *    Copyright (C) 2015 QAware GmbH
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
package de.qaware.chronix.dts;

import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * An operator or function is called weak if it accepts nulls.
 * This class's method produce weak versions of operators and functions.
 *
 * @author johannes.siedersleben
 */
public final class WeakLogic {
    /**
     * Private utility class constructor.
     */
    private WeakLogic() {
    }

    /**
     * @param comparator any comparator
     * @param <T>        any type
     * @return a comparator which accepts null and considers
     * null as the smallest element of the universe
     */
    public static <T> Comparator<T> weakComparator(Comparator<? super T> comparator) {
        return (T x, T y) -> {
            if ((x == null) && (y != null)) {
                return -1;
            } else if (x == null) {
                return 0;
            } else if (y == null) {
                return 1;
            } else {
                return comparator.compare(x, y);
            }
        };
    }

    /**
     * @param <T> any type
     * @return a comparator which compares any two objects including null and considers
     * null as the smallest element of the universe
     */
    public static <T extends Comparable<T>> Comparator<T> weakComparator() {
        return weakComparator(Comparable::compareTo);
    }

    /**
     * @param op  a binary operator
     * @param <T> any type
     * @return a binary operator accepting nulls. It returns the result of op
     * iff neither operand is null, the one which is not null if either is and
     * null if both of them are.
     */
    public static <T> BinaryOperator<T> weakBinaryOperator(BinaryOperator<T> op) {
        return (T x, T y) -> {
            if ((x == null) && (y != null)) {
                return y;
            } else if (x == null) {
                return null;
            } else if (y == null) {
                return x;
            } else {
                return op.apply(x, y);
            }
        };
    }

    /**
     * @param op  a unary operator
     * @param <T> any type
     * @return a unary operator returning the result of op iff the operand is not null
     * and null otherwise
     */
    public static <T> UnaryOperator<T> weakUnaryOperator(UnaryOperator<T> op) {
        return x -> x == null ? null : op.apply(x);
    }

    /**
     * @param f   any function
     * @param <F> any type
     * @param <T> any type
     * @return a function returning f iff the operand is not null and null otherwise.
     */
    public static <F, T> Function<F, T> weakFunction(Function<F, T> f) {
        return x -> x == null ? null : f.apply(x);
    }

    /**
     * @param x   an object
     * @param y   another object
     * @param <T> any type
     * @return true iff they are both null or x.weakEquals(y) otherwise
     */
    public static <T> boolean weakEquals(T x, T y) {
        return x == null ? y == null : x.equals(y);
    }
}
