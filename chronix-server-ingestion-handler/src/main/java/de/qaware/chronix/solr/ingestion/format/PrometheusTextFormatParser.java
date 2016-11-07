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
package de.qaware.chronix.solr.ingestion.format;

import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Parses the Prometheus Text format.
 * <p>
 * See https://prometheus.io/docs/instrumenting/exposition_formats/#text-format-details
 */
public class PrometheusTextFormatParser implements FormatParser {
    /**
     * UTF-8 charset. Used for decoding the given input stream.
     */
    private static final Charset UTF_8 = Charset.forName("utf-8");

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusTextFormatParser.class);

    /**
     * Clock.
     */
    private final Clock clock;

    /**
     * Constructor.
     *
     * @param clock Clock.
     */
    public PrometheusTextFormatParser(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Iterable<MetricTimeSeries> parse(InputStream stream) throws FormatParseException {
        Set<String> validMetricNames = new HashSet<>();

        Map<Metric, MetricTimeSeries.Builder> metrics = new HashMap<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, UTF_8));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                if (isHelpLine(line)) {
                    continue;
                }
                if (isTypeLine(line)) {
                    TypeLine typeLine = parseTypeLine(line);
                    if (isTypeValid(typeLine)) {
                        validMetricNames.add(typeLine.getMetricName());
                    }

                    continue;
                }
                if (isCommentLine(line)) {
                    continue;
                }

                // Example: http_requests_total{method="post",code="200"} 1027 1395066363000
                String[] parts = StringUtils.split(line, ' ');
                // At least 2 parts, because timestamp is optional
                if (parts.length < 2) {
                    throw new FormatParseException("Expected at least 2 parts, found " + parts.length + " in line '" + line + "'");
                }

                String metricName = getMetricName(parts);
                if (!validMetricNames.contains(metricName)) {
                    LOGGER.debug("Ignoring metric {}", metricName);
                    continue;
                }

                Instant timestamp = getMetricTimestamp(parts);
                double value = getMetricValue(parts);
                Map<String, String> tags = getMetricTags(parts);

                addPoint(metrics, metricName, timestamp, value, tags);
            }
        } catch (IOException e) {
            throw new FormatParseException("IO exception while parsing OpenTSDB telnet format", e);
        }

        return metrics.values().stream().map(MetricTimeSeries.Builder::build).collect(Collectors.toList());
    }

    /**
     * Adds a point to the given metrics map. If the metric doesn't exist in the map, it will be created.
     *
     * @param metrics    Metric map.
     * @param metricName Name of the metric.
     * @param timestamp  Timestamp of the point.
     * @param value      Value of the point.
     * @param tags       Tags for the metric. These are only used if the metric doesn't already exist in the metrics map.
     */
    private void addPoint(Map<Metric, MetricTimeSeries.Builder> metrics, String metricName, Instant timestamp, double value, Map<String, String> tags) {
        Metric metric = new Metric(metricName, tags);
        MetricTimeSeries.Builder metricBuilder = metrics.get(metric);
        if (metricBuilder == null) {
            metricBuilder = new MetricTimeSeries.Builder(metricName);
            for (Map.Entry<String, String> tagEntry : tags.entrySet()) {
                metricBuilder.attribute(tagEntry.getKey(), tagEntry.getValue());
            }
            metrics.put(metric, metricBuilder);
        }

        metricBuilder.point(timestamp.toEpochMilli(), value);
    }

    /**
     * Extract the metric tags from the parts.
     *
     * @param parts Parts.
     * @return Metric tags.
     */
    private Map<String, String> getMetricTags(String[] parts) throws FormatParseException {
        String nameWithTags = parts[0];

        int tagStartIndex = nameWithTags.indexOf('{');
        if (tagStartIndex == -1) {
            return Collections.emptyMap();
        }

        String tagString = nameWithTags.substring(tagStartIndex + 1, nameWithTags.indexOf('}'));
        String[] tags = StringUtils.split(tagString, ',');

        Map<String, String> result = new HashMap<>();

        for (String tag : tags) {
            String[] tagParts = StringUtils.split(tag, "=", 2);
            if (tagParts.length != 2) {
                throw new FormatParseException("Expected 2 tag parts, found " + tagParts.length + " in tag '" + tag + "'");
            }
            String tagValue = tagParts[1];
            if (!tagValue.startsWith("\"") && !tagValue.endsWith("\"")) {
                throw new FormatParseException("Expected the tag value between \"s, but it isn't. Tag: '" + tag + "'");
            }

            String tagWithoutQuotes = tagValue.substring(1, tagValue.length() - 1);
            result.put(tagParts[0], tagWithoutQuotes);
        }

        return result;
    }

    /**
     * Extracts the metric timestamp from the parts.
     *
     * @param parts Parts.
     * @return Metric timestamp.
     * @throws FormatParseException If something went wrong while extracting.
     */
    private Instant getMetricTimestamp(String[] parts) throws FormatParseException {
        // If the timestamp is missing, wall clock time is assumed.
        if (parts.length < 3) {
            return clock.now();
        }

        String value = parts[2];
        try {
            long epochTime = Long.parseLong(value);
            return Instant.ofEpochMilli(epochTime);
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
        String nameWithTags = parts[0];

        int tagStartIndex = nameWithTags.indexOf('{');
        if (tagStartIndex == -1) {
            return nameWithTags;
        }

        return nameWithTags.substring(0, tagStartIndex);
    }

    private boolean isTypeValid(TypeLine typeLine) {
        return typeLine.getType().equals("counter");
    }

    /**
     * Parses a type line.
     *
     * @param line Line to parse.
     * @return Parsed type line.
     * @throws FormatParseException If something went wrong while parsing.
     */
    private TypeLine parseTypeLine(String line) throws FormatParseException {
        // Example: TYPE http_requests_total counter
        String[] parts = StringUtils.split(line, " ");
        if (parts.length != 4) {
            throw new FormatParseException("Expected 4 parts in TYPE line, found " + parts.length + " in line '" + line + "'");
        }

        // First two parts are '#' and 'TYPE'
        return new TypeLine(parts[2], parts[3]);
    }

    private boolean isCommentLine(String line) {
        return line.startsWith("#");
    }

    private boolean isTypeLine(String line) {
        return line.startsWith("# TYPE");
    }

    private boolean isHelpLine(String line) {
        return line.startsWith("# HELP");
    }

    /**
     * DTO for a type line.
     */
    private static class TypeLine {
        private final String metricName;
        private final String type;

        public TypeLine(String metricName, String type) {
            this.metricName = metricName;
            this.type = type;
        }

        public String getMetricName() {
            return metricName;
        }

        public String getType() {
            return type;
        }
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
