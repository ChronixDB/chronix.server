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

/**
 * Datatype that represents a pair of values.
 *
 * @param <F> the type of the first element
 * @param <S> the type of the second element
 * @author johannes.siedersleben
 */
public class Pair<F, S> {

    private final F first;
    private final S second;

    /**
     * @param first  first element. May also be null.
     * @param second second element. May also be null.
     */
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Factory method to create a Pair of F and S.
     *
     * @param first  the first
     * @param second the second
     * @param <F>    first type
     * @param <S>    second type
     * @return a new pair
     */
    @SuppressWarnings("unused")
    public static <F, S> Pair<F, S> pairOf(F first, S second) {
        return new Pair<>(first, second);
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    /**
     * @return first and second element as an array
     */
    public Object[] asArray() {
        return new Object[]{first, second};
    }

    /**
     * @return first and second element as a list
     */
    public List<Object> asList() {
        List<Object> li = new ArrayList<>(2);
        li.add(first);
        li.add(second);
        return li;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (first != null) {
            sb.append("first=").append(first.toString()).append(';');
        }
        if (second != null) {
            sb.append("second=").append(second.toString()).append(';');
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Pair pair = (Pair) o;

        return !(first != null ? !first.equals(pair.first) : pair.first != null) &&
                !((second != null) ? !second.equals(pair.second) : (pair.second != null));
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }
}
