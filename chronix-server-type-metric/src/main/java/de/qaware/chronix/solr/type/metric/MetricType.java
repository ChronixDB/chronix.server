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
import de.qaware.chronix.solr.type.metric.functions.aggregations.*;
import de.qaware.chronix.solr.type.metric.functions.analyses.*;
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
public class MetricType implements ChronixType {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricType.class);

    @Override
    public String getType() {
        return "metric";
    }

    @Override
    public ChronixTimeSeries convert(List<SolrDocument> records, long queryStart, long queryEnd, boolean rawDataIsRequested) {
        MetricTimeSeries metricTimeSeries = SolrDocumentBuilder.reduceDocumentToTimeSeries(queryStart, queryEnd, records, rawDataIsRequested);
        return new ChronixMetricTimeSeries(metricTimeSeries);
    }

    @Override
    public ChronixFunction getFunction(String function, String[] args) {

        switch (function) {
            //Aggregations
            case "avg":
                return new Avg();
            case "min":
                return new Min();
            case "max":
                return new Max();
            case "sum":
                return new Sum();
            case "count":
                return new Count();
            case "dev":
                return new StdDev();
            case "last":
                return new Last();
            case "first":
                return new First();
            case "range":
                return new Range();
            case "diff":
                return new Difference();
            case "sdiff":
                return new SignedDifference();
            case "p":
                return new Percentile(args);
            case "integral":
                return new Integral();
            case "trend":
                return new Trend();
            //Transformations
            case "add":
                return new Add(args);
            case "sub":
                return new Subtract(args);
            case "vector":
                return new Vectorization(args);
            case "bottom":
                return new Bottom(args);
            case "top":
                return new Top(args);
            case "movavg":
                return new MovingAverage(args);
            case "smovavg":
                return new SampleMovingAverage(args);
            case "scale":
                return new Scale(args);
            case "divide":
                return new Divide(args);
            case "derivative":
                return new Derivative();
            case "nnderivative":
                return new NonNegativeDerivative();
            case "timeshift":
                return new Timeshift(args);
            case "distinct":
                return new Distinct();
            //Analyses
            case "outlier":
                return new Outlier();
            case "frequency":
                return new Frequency(args);
            case "fastdtw":
                return new FastDtw(args);
            case "sax":
                return new Sax(args);
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
