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

import de.qaware.chronix.server.functions.ChronixAggregation;
import de.qaware.chronix.server.functions.FunctionValueMap;
import de.qaware.chronix.timeseries.MetricTimeSeries;


public class Nonsense implements ChronixAggregation<MetricTimeSeries> {

    public void execute(MetricTimeSeries timeSeries, FunctionValueMap functionValueMap) {
        functionValueMap.add(this, 4711);

    }

    public String getQueryName() {
        return "nonsense";
    }

    public String getTimeSeriesType() {
        return "metric";
    }

}
