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
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Parses the OpenTSDB telnet format.
 * <p>
 * See http://opentsdb.net/docs/build/html/user_guide/writing.html.
 */
public class OpenTsdbTelnetFormatParser implements FormatParser {
    /**
     * UTF-8 charset. Used for decoding the given input stream.
     */
    private static final Charset UTF_8 = Charset.forName("utf-8");

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
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Metric metric = (Metric) o;
            return Objects.equals(name, metric.name) &&
                    Objects.equals(tags, metric.tags);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, tags);
        }
    }

    @Override
    public Iterable<MetricTimeSeries> parse(InputStream stream) throws FormatParseException {
        Map<Metric, MetricTimeSeries.Builder> metrics = new HashMap<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, UTF_8));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                // Format is: put <metric> <timestamp> <value> <tagk1=tagv1[ tagk2=tagv2 ...tagkN=tagvN]>
                // Example: put sys.cpu.user 1356998400 42.5 host=webserver01 cpu=0

                String[] parts = StringUtils.split(line, ' ');
                // 5 parts, because "Each data point must have at least one tag."
                if (parts.length < 5) {
                    throw new FormatParseException("Expected at least 5 parts, found " + parts.length + " in line '" + line + "'");
                }

                if (!parts[0].equals("put")) {
                    throw new FormatParseException("Expected first segment to be 'put', but was '" + parts[0] + "'");
                }

                String metricName = getMetricName(parts);
                Instant timestamp = getMetricTimestamp(parts);
                double value = getMetricValue(parts);
                Map<String, String> tags = getMetricTags(parts);

                // If the metric is already known, add a point. Otherwise create the metric and add the point.
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
        } catch (IOException e) {
            throw new FormatParseException("IO exception while parsing OpenTSDB telnet format", e);
        }

        return metrics.values().stream().map(MetricTimeSeries.Builder::build).collect(Collectors.toList());
    }

    /**
     * Extract the metric tags from the parts.
     *
     * @param parts Parts.
     * @return Metric tags.
     */
    private Map<String, String> getMetricTags(String[] parts) throws FormatParseException {
        Map<String, String> tags = new HashMap<>();

        for (int i = 4; i < parts.length; i++) {
            String tag = parts[i];
            String[] tagParts = StringUtils.split(tag, "=", 2);
            if (tagParts.length != 2) {
                throw new FormatParseException("Expected 2 tag parts, found " + tagParts.length + " in tag '" + tag + "'");
            }

            tags.put(tagParts[0], tagParts[1]);
        }

        return tags;
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
            if (value.length() != 10 && value.length() != 13) {
                throw new FormatParseException("Expected a timestamp length of 10 or 13, found " + value.length() + " ('" + value + "')");
            }

            // 10 digits means seconds, 13 digits mean milliseconds
            boolean secondResolution = value.length() == 10;

            long epochTime = Long.parseLong(value);
            return secondResolution ? Instant.ofEpochSecond(epochTime) : Instant.ofEpochMilli(epochTime);
        } catch (NumberFormatException e) {
            throw new FormatParseException("Can't convert '" + value + "' to long");
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
        String value = parts[3];
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new FormatParseException("Can't convert '" + value + "' to double");
        }
    }

    /**
     * Extracts the metric name from the given parts.
     *
     * @param parts Parts.
     * @return Metric name.
     */
    private String getMetricName(String[] parts) {
        return parts[1];
    }
}
