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
package de.qaware.chronix.solr.type.metric;

import com.google.inject.multibindings.Multibinder;
import de.qaware.chronix.server.types.ChronixType;
import de.qaware.chronix.server.types.ChronixTypePlugin;

/**
 * Guice binding for the metric type
 */
public class MetricTypeModule extends ChronixTypePlugin {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), ChronixType.class).addBinding().to(MetricType.class);
    }
}
