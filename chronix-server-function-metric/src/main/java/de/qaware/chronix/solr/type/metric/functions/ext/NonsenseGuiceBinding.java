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
package de.qaware.chronix.solr.type.metric.functions.ext;

import com.google.inject.multibindings.Multibinder;
import de.qaware.chronix.server.functions.ChronixFunction;
import de.qaware.chronix.server.functions.plugin.ChronixFunctionPlugin;

/**
 * Created by flo on 2/28/17.
 */
public class NonsenseGuiceBinding extends ChronixFunctionPlugin {

    protected void configure() {
        Multibinder.newSetBinder(binder(), ChronixFunction.class).addBinding().to(Nonsense.class);
    }
}
