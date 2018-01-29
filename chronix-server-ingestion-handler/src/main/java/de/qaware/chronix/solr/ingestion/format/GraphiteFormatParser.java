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
package de.qaware.chronix.solr.ingestion.format;

import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A format parser for the graphite line procotol.
 * <p>
 * See http://graphite.readthedocs.io/en/latest/feeding-carbon.html
 */
public class GraphiteFormatParser implements FormatParser {
    /**
     * UTF-8 charset. Used for decoding the given input stream.
     */
    private static final Charset UTF_8 = Charset.forName("utf-8");
    private static final String METRIC_TYPE = "metric";

    @Override
    public Iterable<MetricTimeSeries> parse(InputStream stream) throws FormatParseException {
        Map<String, MetricTimeSeries.Builder> metrics = new HashMap<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, UTF_8));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                // Format is: <metric path> <metric value> <metric timestamp>
                String[] parts = StringUtils.split(line, ' ');
                if (parts.length != 3) {
                    throw new FormatParseException("Expected 3 parts, found " + parts.length + " in line '" + line + "'");
                }

                String metricName = getMetricName(parts);
                double value = getMetricValue(parts);
                Instant timestamp = getMetricTimestamp(parts);

                // If the metric is already known, add a point. Otherwise create the metric and add the point.
                MetricTimeSeries.Builder metricBuilder = metrics.get(metricName);
                if (metricBuilder == null) {
                    metricBuilder = new MetricTimeSeries.Builder(metricName, METRIC_TYPE);
                    metrics.put(metricName, metricBuilder);
                }
                metricBuilder.point(timestamp.toEpochMilli(), value);
            }
        } catch (IOException e) {
            throw new FormatParseException("IO exception while parsing Graphite format", e);
        }

        return metrics.values().stream().map(MetricTimeSeries.Builder::build).collect(Collectors.toList());
    }

    /**
     * Extracts the metric timestamp from the parts.
     *
     * @param parts Parts.
     * @return Metric timestamp.
     * @throws FormatParseException If something went wrong while extracting.
     */
    private Instant getMetricTimestamp(String[] parts) throws FormatParseException {
        String value = parts[2];
        try {
            long epochTime = Long.parseLong(value);
            return Instant.ofEpochSecond(epochTime);
        } catch (NumberFormatException e) {
            throw new FormatParseException("Can't convert '" + value + "' to long", e);
        }
    }

    /**
     * Extracts the metric value from the given parts.
     *
     * @param parts Parts.
     * @return Metric value.
     * @throws FormatParseException If something went wrong while extracting.
     */
    private double getMetricValue(String[] parts) throws FormatParseException {
        String value = parts[1];
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new FormatParseException("Can't convert '" + value + "' to double", e);
        }
    }

    /**
     * Extracts the metric name from the given parts.
     *
     * @param parts Parts.
     * @return Metric name.
     */
    private String getMetricName(String[] parts) {
        return parts[0];
    }
}
