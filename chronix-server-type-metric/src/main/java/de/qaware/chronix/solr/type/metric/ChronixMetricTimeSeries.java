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
package de.qaware.chronix.solr.type.metric;

import de.qaware.chronix.converter.common.Compression;
import de.qaware.chronix.converter.serializer.json.JsonMetricTimeSeriesSerializer;
import de.qaware.chronix.converter.serializer.protobuf.ProtoBufMetricTimeSeriesSerializer;
import de.qaware.chronix.server.functions.*;
import de.qaware.chronix.server.types.ChronixTimeSeries;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.solr.common.util.Pair;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * Implementation of the chronix time series interface for the metric time series
 *
 * @author f.lautenschlager
 */
public class ChronixMetricTimeSeries implements ChronixTimeSeries {

    private MetricTimeSeries timeSeries;

    /**
     * @param metricTimeSeries the wrapped time series
     */
    ChronixMetricTimeSeries(MetricTimeSeries metricTimeSeries) {
        timeSeries = metricTimeSeries;
    }

    @Override
    public void applyTransformation(ChronixTransformation transformation, FunctionValueMap functionValues) {
        transformation.execute(timeSeries, functionValues);
    }

    @Override
    public void applyAggregation(ChronixAggregation aggregation, FunctionValueMap functionValues) {
        aggregation.execute(timeSeries, functionValues);
    }

    @Override
    public void applyAnalysis(ChronixAnalysis function, FunctionValueMap functionValueMap) {
        function.execute(timeSeries, functionValueMap);
    }

    @Override
    public void applyPairAnalysis(ChronixPairAnalysis analysis, ChronixTimeSeries subQueryTimeSeries, FunctionValueMap functionValues) {
        ChronixPairAnalysis<Pair<MetricTimeSeries, MetricTimeSeries>> pairAnalysis = ((ChronixPairAnalysis<Pair<MetricTimeSeries, MetricTimeSeries>>) analysis);

        ChronixMetricTimeSeries secondMetricTimeSeries = (ChronixMetricTimeSeries) subQueryTimeSeries;
        pairAnalysis.execute(new Pair(timeSeries, secondMetricTimeSeries.timeSeries), functionValues);
    }

    @Override
    public String getType() {
        return timeSeries.getType();
    }

    @Override
    public String getName() {
        return timeSeries.getName();
    }

    @Override
    public long getStart() {
        return timeSeries.getStart();
    }

    @Override
    public long getEnd() {
        return timeSeries.getEnd();
    }

    @Override
    public Map<String, Object> attributes() {
        return timeSeries.getAttributesReference();
    }

    @Override
    public void sort() {
        timeSeries.sort();
    }

    @Override
    public String dataAsJson() {
        byte[] data = new JsonMetricTimeSeriesSerializer().toJson(timeSeries);
        return new String(data, Charset.forName("UTF-8"));
    }

    @Override
    public byte[] dataAsBlob() {
        byte[] data = ProtoBufMetricTimeSeriesSerializer.to(timeSeries.points().iterator());
        //compress data
        return Compression.compress(data);
    }
}
