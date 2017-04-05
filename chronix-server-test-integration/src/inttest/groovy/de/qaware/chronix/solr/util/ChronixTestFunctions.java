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
package de.qaware.chronix.solr.util;

import de.qaware.chronix.converter.common.DoubleList;
import de.qaware.chronix.converter.common.LongList;
import de.qaware.chronix.timeseries.MetricTimeSeries;

import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * Class with test functions for the integration test.
 *
 * @author f.lautenschlager
 */
public final class ChronixTestFunctions {

    public static final Function<MetricTimeSeries, String> GROUP_BY = ts -> ts.attribute("host") + "-" +
            ts.attribute("source") + "-" +
            ts.attribute("group") + "-" +
            ts.getType() + "-" +
            ts.getName();
    public static final BinaryOperator<MetricTimeSeries> REDUCE = (t1, t2) -> {
        MetricTimeSeries.Builder reduced = new MetricTimeSeries.Builder(t1.getName(), t1.getType())
                .points(concat(t1.getTimestamps(), t2.getTimestamps()),
                        concat(t1.getValues(), t2.getValues()))
                .attributes(t1.attributes());
        return reduced.build();
    };

    private ChronixTestFunctions() {
        //avoid instances
    }

    private static DoubleList concat(DoubleList first, DoubleList second) {
        first.addAll(second);
        return first;
    }

    private static LongList concat(LongList first, LongList second) {
        first.addAll(second);
        return first;
    }
}
