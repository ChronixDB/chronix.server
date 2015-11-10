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
package de.qaware.chronix.iterators;

import java.util.Iterator;

/**
 * Iterators of this class do not support remove.
 *
 * @param <T> any type
 * @author johannes.siedersleben
 */
public interface ImmutableIterator<T> extends Iterator<T> {
    /**
     * Remove is not allowed for immutable iterators.
     */
    default void remove() {
        throw new UnsupportedOperationException();
    }
}

