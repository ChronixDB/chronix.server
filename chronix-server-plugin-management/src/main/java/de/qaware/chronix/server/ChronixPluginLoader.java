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
package de.qaware.chronix.server;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import java.util.ServiceLoader;

/**
 * Loads Chronix plugins of the given type.
 *
 * @param <M> the module type to install
 */
public final class ChronixPluginLoader<M extends Module> extends AbstractModule {

    private final Class<M> type;

    private ChronixPluginLoader(Class<M> type) {
        this.type = type;
    }

    public static <M extends Module> ChronixPluginLoader<M> of(Class<M> type) {
        return new ChronixPluginLoader<>(type);
    }

    @Override
    protected void configure() {
        for (M module : ServiceLoader.load(type, type.getClassLoader())) {
            install(module);
        }
    }
}
