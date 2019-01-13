/*
 * Copyright (C) 2018 QAware GmbH
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

import de.qaware.chronix.server.types.ChronixTimeSeries;

import java.util.List;

/**
 * The generic Chronix function interface
 *
 * @param <T> the type of the time series
 */
public interface ChronixFunction<T> {
    /**
     * Executes a Chronix function on the given time series. The result should be added to the function value map.
     *
     * @param functionCtx    context holding the function values
     * @param timeSeriesList the time series list with all time series
     */
    void execute(List<ChronixTimeSeries<T>> timeSeriesList, FunctionCtx functionCtx);

    /**
     * The arguments
     *
     * @param args the args as strings
     */
    default void setArguments(String[] args) {
        //do nothing
    }

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
    String getQueryName();

    /**
     * @return the type of the time series the function belongs to
     */
    String getType();

    /**
     * @return the type of the function
     */
    FunctionType getFunctionType();

    enum FunctionType {
        AGGREGATION,
        TRANSFORMATION,
        ANALYSIS,
        FILTER
    }
}
