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

/**
 * @param <T> the type to apply the analysis on
 * @author f.lautenschlager
 */
public interface ChronixAnalysis<T> extends ChronixFunction<T> {

    /**
     * @return if the analysis needs a sub query. Default is false
     */
    default boolean needSubquery() {
        return false;
    }

    /**
     * @return the sub query of the analysis. Default is null.
     */
    default String getSubquery() {
        return null;
    }


    @Override
    default FunctionType getFunctionType() {
        return FunctionType.ANALYSIS;
    }
}
