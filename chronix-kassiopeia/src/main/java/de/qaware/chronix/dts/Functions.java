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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Utility class to work with Java8 functions.
 *
 * @author johannes.siedersleben
 */
public final class Functions {

    /**
     * Private utility class constructor.
     */
    private Functions() {
    }

    /**
     * @param f   first function to be composed; f : S -> T
     * @param g   second function to be composed; g : R - > S
     * @param <R> any type
     * @param <S> any type
     * @param <T> any type
     * @return composition of f and g; f 0 g : R -> T
     */
    public static <R, S, T> Function<R, T> compose(Function<S, T> f, Function<R, S> g) {
        return x -> f.apply(g.apply(x));
    }

    /**
     * @param f    the function to be curried
     * @param args fixed arguments  (there number is n)
     * @param <F>  type of the argument (From)
     * @param <T>  type of the result (To)
     * @return a function having the first n arguments fixed
     */
    public static <F, T> Function<List<F>, T> curryLeft(Function<List<F>, T> f, List<F> args) {
        return (List<F> xs) -> {
            List<F> aux = new ArrayList<>();
            aux.addAll(args);
            aux.addAll(xs);
            return f.apply(aux);
        };
    }

    /**
     * @param f   a binary function
     * @param x   the value to be left curried
     * @param <E> any type
     * @param <F> any type
     * @param <T> any type
     * @return a unary function
     */
    public static <E, F, T> Function<F, T> curryLeft(BiFunction<E, F, T> f, E x) {
        return y -> f.apply(x, y);
    }

    /**
     * @param f   a binary function
     * @param y   the value to be right curried
     * @param <E> any type
     * @param <F> any type
     * @param <T> any type
     * @return a unary function
     */
    public static <E, F, T> Function<E, T> curryRight(BiFunction<E, F, T> f, F y) {
        return x -> f.apply(x, y);
    }

    /**
     * @param f    the function to be curried
     * @param args fixed arguments (there number is n)
     * @param <F>  type of the argument (From)
     * @param <T>  type of the result (To)
     * @return a function having the last n arguments fixed
     */
    public static <F, T> Function<List<F>, T> curryRight(Function<List<F>, T> f, List<F> args) {
        return (List<F> xs) -> {
            List<F> aux = new ArrayList<>();
            aux.addAll(xs);
            aux.addAll(args);
            return f.apply(aux);
        };
    }
}
