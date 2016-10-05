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
package de.qaware.chronix.solr.query.analysis.functions;

/**
 * The generic Chronix function interface
 *
 * @param <T> the type of the time series
 */
public interface ChronixFunction<T> {
    /**
     * Executes a Chronix function on the given time series. The result should be added to the function value map.
     *
     * @param timeSeries       the time series as argument for the chronix function
     * @param functionValueMap the analysis and values result map
     */
    void execute(T timeSeries, FunctionValueMap functionValueMap);

    /**
     * Gets the arguments of the function. Default is an empty string array.
     *
     * @return the arguments
     */
    default String[] getArguments() {
        return new String[0];
    }

    /**
     * @return the type of the analysis
     */
    FunctionType getType();
}
