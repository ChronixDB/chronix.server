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

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.qaware.chronix.server.ChronixPluginLoader;
import de.qaware.chronix.server.functions.plugin.ChronixFunctionPlugin;
import de.qaware.chronix.server.functions.plugin.ChronixFunctions;
import de.qaware.chronix.server.types.ChronixTypePlugin;
import de.qaware.chronix.server.types.ChronixTypes;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Fork(value = 2)
public class CQLJMHBenchmarkTest {

    private static final String[] CQL_EXPRESSIONS = new String[]{
            "metric{min;max;avg}",
            "metric{max;avg;min;dev;count}",
            "metric{first;integral}",
            "metric{last;first;range;sdiff;p:0.4}",
            "metric{min;max;avg;top:10;trend}",
            "metric{min;count;avg;sdiff}"
    };

    private static final Injector INJECTOR = Guice.createInjector(
            ChronixPluginLoader.of(ChronixTypePlugin.class),
            ChronixPluginLoader.of(ChronixFunctionPlugin.class));
    private static final ChronixTypes TYPES = INJECTOR.getInstance(ChronixTypes.class);
    private static final ChronixFunctions FUNCTIONS = INJECTOR.getInstance(ChronixFunctions.class);
    private static final CQL CQL = new CQL(TYPES, FUNCTIONS);

    @Benchmark
    @Measurement(iterations = 10, time = 5)
    @Warmup(iterations = 1)
    public void cqlReuse(Blackhole blackhole) {
        for (String cqlExpression : CQL_EXPRESSIONS) {
            blackhole.consume(CQL.parseCF(cqlExpression));
        }
    }

    @Benchmark
    @Measurement(iterations = 10, time = 5)
    @Warmup(iterations = 1)
    public void cqlNewInstance(Blackhole blackhole) {
        for (String cqlExpression : CQL_EXPRESSIONS) {
            blackhole.consume(new CQL(TYPES, FUNCTIONS).parseCF(cqlExpression));
        }
    }
}
