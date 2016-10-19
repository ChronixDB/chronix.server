/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.ingestion.format;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.qaware.chronix.solr.ingestion.format.opentsdb.TsdbMetric;
import de.qaware.chronix.timeseries.MetricTimeSeries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A format parser for the OpenTSDB HTTP API procotol.
 * <p>
 * See http://opentsdb.net/docs/build/html/api_http/put.html
 */
public class OpenTsdbHttpFormatParser implements FormatParser {
    /**
     * UTF-8 charset. Used for decoding the given input stream.
     */
    private static final Charset UTF_8 = Charset.forName("utf-8");

    @Override
    public Iterable<MetricTimeSeries> parse(InputStream stream) throws FormatParseException {
        Map<Metric, MetricTimeSeries.Builder> metrics = new HashMap<>();

        List<TsdbMetric> tsdbMetrics = parseJson(stream);

        for (TsdbMetric tsdbMetric : tsdbMetrics) {
            // If the metric is already known, add a point. Otherwise create the metric and add the point.
            Metric metric = new Metric(tsdbMetric.getMetric(), tsdbMetric.getTags());
            MetricTimeSeries.Builder metricBuilder = metrics.get(metric);
            if (metricBuilder == null) {
                metricBuilder = new MetricTimeSeries.Builder(tsdbMetric.getMetric());
                for (Map.Entry<String, String> tagEntry : tsdbMetric.getTags().entrySet()) {
                    metricBuilder.attribute(tagEntry.getKey(), tagEntry.getValue());
                }
                metrics.put(metric, metricBuilder);
            }

            Instant timestamp = convertTimestamp(tsdbMetric.getTimestamp());
            metricBuilder.point(timestamp.toEpochMilli(), tsdbMetric.getValue());
        }

        return metrics.values().stream().map(MetricTimeSeries.Builder::build).collect(Collectors.toList());
    }

    /**
     * Parses the given stream into a list of {@link TsdbMetric}s.
     * <p>
     * Supports parsing a single and multiple metrics. If a single metric is parsed, a list with one element is returned.
     *
     * @param stream Stream.
     * @return Parsed metrics.
     * @throws FormatParseException If something went wrong while parsing.
     */
    private List<TsdbMetric> parseJson(InputStream stream) throws FormatParseException {
        Gson gson = new Gson();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, UTF_8));
        List<TsdbMetric> tsdbMetrics;
        if (containsSingleMetric(reader)) {
            TsdbMetric metric = gson.fromJson(reader, TsdbMetric.class);
            tsdbMetrics = Collections.singletonList(metric);
        } else {
            tsdbMetrics = gson.fromJson(reader, new TypeToken<List<TsdbMetric>>() {
            }.getType());
        }
        return tsdbMetrics;
    }

    /**
     * Converts the timestamp to an Instant.
     * <p>
     * Supports 10 digit timestamps (second resolution) and 13 digit timestamps (millisecond resolution).
     *
     * @param timestamp Timestamp.
     * @return Instant.
     * @throws FormatParseException If the timestamp doesn't have 10 or 13 digits.
     */
    private Instant convertTimestamp(long timestamp) throws FormatParseException {
        if (timestamp == 0) {
            return Instant.ofEpochSecond(timestamp);
        }

        int digits = (int) (Math.log10(timestamp) + 1);
        if (digits != 10 && digits != 13) {
            throw new FormatParseException("Expected a timestamp length of 10 or 13, found " + digits + " ('" + timestamp + "')");
        }

        // 10 digits means seconds, 13 digits mean milliseconds
        boolean secondResolution = digits == 10;
        return secondResolution ? Instant.ofEpochSecond(timestamp) : Instant.ofEpochMilli(timestamp);
    }

    /**
     * Determines if the given reader contains a single or multiple metrics.
     *
     * @param reader Reader.
     * @return True if the reader contains a single metric, false if the reader contains multiple.
     * @throws FormatParseException If something went wrong.
     */
    private boolean containsSingleMetric(BufferedReader reader) throws FormatParseException {
        try {
            // Need to reset the reader after reading the first char
            reader.mark(1);
            char firstChar = (char) reader.read();
            reader.reset();

            // If '{', single metric. Multiple metrics start with '['
            return firstChar == '{';
        } catch (IOException e) {
            throw new FormatParseException("IOException while determining if single or multiple metrics ", e);
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
