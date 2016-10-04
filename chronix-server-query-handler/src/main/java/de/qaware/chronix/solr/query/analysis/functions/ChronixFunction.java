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

import de.qaware.chronix.solr.query.analysis.FunctionValueMap;

/**
 * Created by f.lautenschlager on 04.10.2016.
 */
public interface ChronixFunction<T> {

    void execute(T timeSeries, FunctionValueMap analysisAndValues);

    /**
     * @return the arguments
     */
    String[] getArguments();

    /**
     * @return the type of the analysis
     */
    FunctionType getType();
}
