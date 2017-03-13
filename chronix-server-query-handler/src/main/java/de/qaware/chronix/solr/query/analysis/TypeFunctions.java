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
package de.qaware.chronix.solr.query.analysis;

import de.qaware.chronix.server.types.ChronixType;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the functions per type
 */
public class TypeFunctions {

    private final Map<ChronixType, QueryFunctions> typeFunctions = new HashMap<>();

    public QueryFunctions getTypeFunctions(ChronixType type) {
        return typeFunctions.getOrDefault(type, null);
    }

    public void setTypeFunctions(ChronixType type, QueryFunctions functions) {
        if (typeFunctions.containsKey(type)) {
            typeFunctions.get(type).merge(functions);
        } else {
            typeFunctions.put(type, functions);
        }
    }

    public boolean isEmpty() {
        return typeFunctions.isEmpty();
    }
}
