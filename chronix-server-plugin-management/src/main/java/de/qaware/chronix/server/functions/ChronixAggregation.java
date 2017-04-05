/*
 * Copyright (C) 2016 QAware GmbH
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
package de.qaware.chronix.server.functions;

/***
 * A class holding the parsed analysis with its arguments
 *
 * @param <T> the type on which the aggregation is applied
 * @author f.lautenschlager
 */
public interface ChronixAggregation<T> extends ChronixFunction<T> {


    @Override
    default FunctionType getType() {
        return FunctionType.AGGREGATION;
    }
}
