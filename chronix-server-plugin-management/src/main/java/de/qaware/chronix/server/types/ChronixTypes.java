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
package de.qaware.chronix.server.types;

import com.google.inject.Inject;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Set;

/**
 * Holds all plugged-in types
 */
public final class ChronixTypes {

    private Set<ChronixType> chronixTypes;

    @Inject
    public ChronixTypes(Set<ChronixType> chronixTypes) {
        this.chronixTypes = chronixTypes;
    }

    /**
     * @param typeName the name of the type
     * @return the type for the name, otherwise null
     */
    public ChronixType getTypeForName(String typeName) {
        for (ChronixType type : chronixTypes) {
            if (type.getType().equalsIgnoreCase(typeName)) {
                return type;
            }
        }
        return null;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("chronixPlugins", chronixTypes)
                .toString();
    }
}
