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
package de.qaware.chronix.server

import com.google.inject.Guice
import de.qaware.chronix.server.functions.plugin.ChronixFunctionPlugin
import de.qaware.chronix.server.functions.plugin.ChronixFunctions
import de.qaware.chronix.server.types.ChronixTypePlugin
import de.qaware.chronix.server.types.ChronixTypes
import spock.lang.Specification

/**
 * Test the plugin mechanism
 * @author f.lautenschlager
 */
class PluginTest extends Specification {


    def "test plugged-in types"() {

        given:
        def injector = Guice.createInjector(ChronixPluginLoader.of(ChronixTypePlugin.class))
        def types = injector.getInstance(ChronixTypes.class)

        when:
        def type = types.getTypeForName("metric")

        then:
        type != null

    }

    def "test plugged-in functions"() {

        given:
        def injector = Guice.createInjector(ChronixPluginLoader.of(ChronixFunctionPlugin.class))

        def functions = injector.getInstance(ChronixFunctions.class)

        when:

        def function = functions.getFunctionForQueryName("metric", "noop")

        then:
        function != null
        function.queryName == "noop"
    }
}
