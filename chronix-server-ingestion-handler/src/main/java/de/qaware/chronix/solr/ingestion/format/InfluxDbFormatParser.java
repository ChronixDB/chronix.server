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
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A format parser for the InfluxDB line protocol.
 * <p>
 * See https://docs.influxdata.com/influxdb/v1.0/write_protocols/line_protocol_reference/
 */
public class InfluxDbFormatParser implements FormatParser {
    /**
     * UTF-8 charset. Used for decoding the given input stream.
     */
    private static final Charset UTF_8 = Charset.forName("utf-8");
    private static final String METRIC_TYPE = "metric";


    private final Clock clock;

    /**
     * Constructor.
     *
     * @param clock Clock.
     */
    public InfluxDbFormatParser(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Iterable<MetricTimeSeries> parse(InputStream stream) throws FormatParseException {
        Map<Metric, MetricTimeSeries.Builder> metrics = new HashMap<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, UTF_8));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                // Format is: {metric},[{tag1}={value1},{tag2}={value2}] value={value} [nanosecond-timestamp]
                // Example: cpu_load_short,host=server02,region=us-west value=0.55 1422568543702900257

                String[] parts = StringUtils.split(line, ' ');
                // 2 parts: metric and value. Timestamp and tags are optional.
                if (parts.length < 2) {
                    throw new FormatParseException("Expected at least 2 parts, found " + parts.length + " in line '" + line + "'");
                }

                String metricName = getMetricName(parts);
                Map<String, String> tags = getMetricTags(parts);
                double value = getMetricValue(parts);
                Instant timestamp = getMetricTimestamp(parts);

                // If the metric is already known, add a point. Otherwise create the metric and add the point.
                Metric metric = new Metric(metricName, tags);
                MetricTimeSeries.Builder metricBuilder = metrics.get(metric);
                if (metricBuilder == null) {
                    metricBuilder = new MetricTimeSeries.Builder(metricName, METRIC_TYPE);
                    for (Map.Entry<String, String> tagEntry : tags.entrySet()) {
                        metricBuilder.attribute(tagEntry.getKey(), tagEntry.getValue());
                    }
                    metrics.put(metric, metricBuilder);
                }

                metricBuilder.point(timestamp.toEpochMilli(), value);
            }
        } catch (IOException e) {
            throw new FormatParseException("IO exception while parsing OpenTSDB telnet format", e);
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
        // Timestamp is optional. If it's missing, use the local server time
        if (parts.length < 3) {
            return clock.now();
        }

        String value = parts[2];
        try {
            long epochTime = Long.parseLong(value);

            // epochTime is in nanoseconds, convert to milliseconds
            return Instant.ofEpochMilli(epochTime / 1000);
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

        String[] valueParts = StringUtils.split(value, "=", 2);
        if (valueParts.length < 2) {
            throw new FormatParseException("Expected at least 2 parts, found " + valueParts.length + " in '" + value + "'");
        }
        if (!valueParts[0].equals("value")) {
            throw new FormatParseException("Expected first part to be 'value', but was '" + valueParts[0] + "' in '" + value + "'");
        }

        try {
            return Double.parseDouble(valueParts[1]);
        } catch (NumberFormatException e) {
            throw new FormatParseException("Can't convert '" + value + "' to double", e);
        }

    }

    /**
     * Extract the metric tags from the parts.
     *
     * @param parts Parts.
     * @return Metric tags.
     * @throws FormatParseException If something went wrong while extracting.
     */
    private Map<String, String> getMetricTags(String[] parts) throws FormatParseException {
        Map<String, String> tags = new HashMap<>();

        String value = parts[0];
        String[] valueParts = StringUtils.split(value, ',');
        for (int i = 1; i < valueParts.length; i++) {
            String tag = valueParts[i];
            String[] tagParts = StringUtils.split(tag, "=", 2);
            if (tagParts.length < 2) {
                throw new FormatParseException("Expected at least 2 parts, found " + tagParts.length + " in '" + tag + "'");
            }
            tags.put(tagParts[0], tagParts[1]);
        }

        return tags;
    }

    /**
     * Extracts the metric name from the given parts.
     *
     * @param parts Parts.
     * @return Metric name.
     * @throws FormatParseException If something went wrong while extracting.
     */
    private String getMetricName(String[] parts) throws FormatParseException {
        String value = parts[0];
        String[] nameParts = StringUtils.split(value, ',');
        if (nameParts.length < 1) {
            throw new FormatParseException("Expected at least 1 part, found " + nameParts.length + " in '" + value + "'");
        }

        return nameParts[0];
    }

    /**
     * DTO for a metric.
     * <p>
     * A metric is unique on its name and tags.
     */
    private static class Metric {
        private final String name;
        private final Map<String, String> tags;

        public Metric(String name, Map<String, String> tags) {
            this.name = name;
            this.tags = tags;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Metric metric = (Metric) o;
            return Objects.equals(name, metric.name) &&
                    Objects.equals(tags, metric.tags);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, tags);
        }
    }
}
