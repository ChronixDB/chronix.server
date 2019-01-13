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
package de.qaware.chronix.cql;

import de.qaware.chronix.server.types.ChronixType;

import java.util.HashMap;
import java.util.Map;

/**
 * The CQLCFResult holds the chronix functions per type.
 */
public class CQLCFResult {

    private final Map<ChronixType, ChronixFunctions> typeFunctions = new HashMap<>();

    /**
     * The Chronix Type to add the Chronix Functions for
     *
     * @param type      to add the Chronix functions for
     * @param functions the Chronix functions
     */
    void addChronixFunctionsForType(ChronixType type, ChronixFunctions functions) {
        if (typeFunctions.containsKey(type)) {
            typeFunctions.get(type).addAll(functions);
        } else {
            typeFunctions.put(type, functions);
        }
    }

    /**
     * Gets the Chronix Functions for a given Chronix type.
     *
     * @param type the chronix type
     * @return the functions or null if the type does not exist.
     */
    public ChronixFunctions getChronixFunctionsForType(ChronixType type) {
        return typeFunctions.get(type);
    }

    /**
     * @return true if empty, otherwise false
     */
    public boolean isEmpty() {
        return typeFunctions.isEmpty();
    }
}
