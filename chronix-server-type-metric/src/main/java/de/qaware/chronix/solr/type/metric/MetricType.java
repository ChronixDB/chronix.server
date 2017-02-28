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

import de.qaware.chronix.server.functions.ChronixFunction;
import de.qaware.chronix.server.types.ChronixTimeSeries;
import de.qaware.chronix.server.types.ChronixType;
import de.qaware.chronix.solr.type.metric.functions.MetricFunctions;
import de.qaware.chronix.solr.type.metric.functions.aggregations.*;
import de.qaware.chronix.solr.type.metric.functions.analyses.FastDtw;
import de.qaware.chronix.solr.type.metric.functions.analyses.Frequency;
import de.qaware.chronix.solr.type.metric.functions.analyses.Outlier;
import de.qaware.chronix.solr.type.metric.functions.analyses.Trend;
import de.qaware.chronix.solr.type.metric.functions.transformation.*;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Implementation of the metric type
 *
 * @author f.lautenschlager
 */
public class MetricType implements ChronixType<MetricTimeSeries> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricType.class);

    @Override
    public String getType() {
        return ChronixQueryParams.TYPE_NAME;
    }

    @Override
    public ChronixTimeSeries convert(List<SolrDocument> records, long queryStart, long queryEnd, boolean rawDataIsRequested) {
        MetricTimeSeries metricTimeSeries = SolrDocumentBuilder.reduceDocumentToTimeSeries(queryStart, queryEnd, records, rawDataIsRequested);
        return new ChronixMetricTimeSeries(metricTimeSeries);
    }

    @Override
    public boolean supportsFunction(String function) {
        return MetricFunctions.ALL_FUNCTIONS.indexOf(function) >= 0;
    }

    @Override
    public ChronixFunction getFunction(String function, String[] args) {

        switch (function) {
            //Aggregations
            case "AVG":
                return new Avg();
            case "MIN":
                return new Min();
            case "MAX":
                return new Max();
            case "SUM":
                return new Sum();
            case "COUNT":
                return new Count();
            case "DEV":
                return new StdDev();
            case "LAST":
                return new Last();
            case "FIRST":
                return new First();
            case "RANGE":
                return new Range();
            case "DIFF":
                return new Difference();
            case "SDIFF":
                return new SignedDifference();
            case "P":
                return new Percentile(args);
            case "INTEGRAL":
                return new Integral();
            case "TREND":
                return new Trend();
            //Transformations
            case "ADD":
                return new Add(args);
            case "SUB":
                return new Subtract(args);
            case "VECTOR":
                return new Vectorization(args);
            case "BOTTOM":
                return new Bottom(args);
            case "TOP":
                return new Top(args);
            case "MOVAVG":
                return new MovingAverage(args);
            case "SMOVAVG":
                return new SampleMovingAverage(args);
            case "SCALE":
                return new Scale(args);
            case "DIVIDE":
                return new Divide(args);
            case "DERIVATIVE":
                return new Derivative();
            case "NNDERIVATIVE":
                return new NonNegativeDerivative();
            case "TIMESHIFT":
                return new Timeshift(args);
            case "DISTINCT":
                return new Distinct();
            //Analyses
            case "OUTLIER":
                return new Outlier();
            case "FREQUENCY":
                return new Frequency(args);
            case "FASTDTW":
                return new FastDtw(args);
            default:
                LOGGER.warn("Ignoring {} as an aggregation. {} is unknown", function, function);
                return null;
        }
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        MetricType rhs = (MetricType) obj;
        return new EqualsBuilder()
                .append(this.getType(), rhs.getType())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getType())
                .toHashCode();
    }
}
