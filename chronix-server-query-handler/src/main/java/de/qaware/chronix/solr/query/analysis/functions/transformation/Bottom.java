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
package de.qaware.chronix.solr.query.analysis.functions.transformation;

import de.qaware.chronix.solr.query.analysis.functions.ChronixTransformation;
import de.qaware.chronix.solr.query.analysis.functions.FunctionType;
import de.qaware.chronix.solr.query.analysis.functions.math.NElements;
import de.qaware.chronix.timeseries.MetricTimeSeries;

/**
 * Bottom transformation get the n bottom values
 *
 * @author f.lautenschlager
 */
public class Bottom implements ChronixTransformation<MetricTimeSeries> {

    private final int n;

    /**
     * Constructs the bottom n values transformation
     *
     * @param n the threshold for the lowest values
     */
    public Bottom(int n) {
        this.n = n;
    }

    @Override
    public MetricTimeSeries transform(MetricTimeSeries timeSeries) {
        NElements.NElementsResult result = NElements.calc(NElements.NElementsCalculation.BOTTOM, n, timeSeries.getTimestampsAsArray(), timeSeries.getValuesAsArray());

        //remove old time series
        timeSeries.clear();
        timeSeries.addAll(result.getNTimes(), result.getNValues());

        return timeSeries;
    }


    @Override
    public FunctionType getType() {
        return FunctionType.BOTTOM;
    }

    @Override
    public String[] getArguments() {
        return new String[]{"n=" + n};
    }

}
