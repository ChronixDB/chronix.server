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

import de.qaware.chronix.Schema;
import de.qaware.chronix.converter.common.Compression;
import de.qaware.chronix.converter.common.MetricTSSchema;
import de.qaware.chronix.converter.serializer.json.JsonMetricTimeSeriesSerializer;
import de.qaware.chronix.converter.serializer.protobuf.ProtoBufMetricTimeSeriesSerializer;
import de.qaware.chronix.server.functions.ChronixAggregation;
import de.qaware.chronix.server.functions.ChronixAnalysis;
import de.qaware.chronix.server.functions.ChronixFunction;
import de.qaware.chronix.server.functions.FunctionValueMap;
import de.qaware.chronix.server.types.ChronixTimeSeries;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.solr.common.SolrDocument;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by flo on 1/30/17.
 */
public class ChronixMetricTimeSeries implements ChronixTimeSeries {

    private MetricTimeSeries timeSeries;

    public ChronixMetricTimeSeries(MetricTimeSeries metricTimeSeries) {
        timeSeries = metricTimeSeries;
    }

    @Override
    public void applyTransformation(ChronixFunction transformation, FunctionValueMap functionValues) {
        transformation.execute(timeSeries, functionValues);
    }

    @Override
    public void applyAggregation(ChronixAggregation aggregation, FunctionValueMap functionValues) {
        aggregation.execute(timeSeries, functionValues);
    }

    @Override
    public void applyAnalysis(ChronixFunction analysis, FunctionValueMap functionValueMap) {
        analysis.execute(timeSeries, functionValueMap);
    }

    @Override
    public void applyPairAnalysis(ChronixAnalysis analysis, ChronixTimeSeries subQueryTimeSeries, FunctionValueMap functionValues) {
        analysis.execute(timeSeries, functionValues);
    }

    @Override
    public SolrDocument convert(String key, boolean dataShouldReturned, boolean dataAsJson) {
        SolrDocument doc = new SolrDocument();

        if (dataShouldReturned) {
            byte[] data;
            //ensure that the returned data is sorted
            timeSeries.sort();
            //data should returned serialized as json
            if (dataAsJson) {
                data = new JsonMetricTimeSeriesSerializer().toJson(timeSeries);
                doc.setField(ChronixQueryParams.DATA_AS_JSON, new String(data, Charset.forName("UTF-8")));
            } else {
                data = ProtoBufMetricTimeSeriesSerializer.to(timeSeries.points().iterator());
                //compress data
                data = Compression.compress(data);
                doc.addField(Schema.DATA, data);
            }
        }

        timeSeries.attributes().forEach(doc::addField);
        //add the metric field as it is not stored in the attributes
        doc.addField(MetricTSSchema.METRIC, timeSeries.getMetric());
        doc.addField(Schema.START, timeSeries.getStart());
        doc.addField(Schema.END, timeSeries.getEnd());

        return doc;
    }

    @Override
    public String getType() {
        return ChronixQueryParams.TYPE_NAME;
    }

    @Override
    public String getName() {
        return timeSeries.getMetric();
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
