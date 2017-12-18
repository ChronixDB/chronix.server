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
package de.qaware.chronix.server.functions.plugin;

import com.google.inject.Inject;
import de.qaware.chronix.server.functions.ChronixFunction;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The plugged in functions
 *
 * @author f.lautenschlager
 */
public final class ChronixFunctions {

    private Map<String, Set<ChronixFunction>> typePluginFunctions = new HashMap<>();

    /**
     * @param chronixPlugins the plugged-in functions
     */
    @Inject
    ChronixFunctions(Set<ChronixFunction> chronixPlugins) {
        for (ChronixFunction pluginFunction : chronixPlugins) {
            if (!typePluginFunctions.containsKey(pluginFunction.getType())) {
                typePluginFunctions.put(pluginFunction.getType(), new HashSet<>());
            }
            typePluginFunctions.get(pluginFunction.getType()).add(pluginFunction);
        }
    }


    /**
     * @param queryName the query name of the function
     * @return the function for the query name, otherwise null
     */
    public ChronixFunction getFunctionForQueryName(String timeSeriesType, String queryName) {
        if (typePluginFunctions.containsKey(timeSeriesType)) {
            for (ChronixFunction function : typePluginFunctions.get(timeSeriesType)) {
                if (function.getQueryName().equals(queryName)) {
                    return function;
                }
            }
        }
        return null;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("chronixPlugins", typePluginFunctions)
                .toString();
    }
}
