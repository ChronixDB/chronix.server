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
import de.qaware.chronix.solr.ingestion.format.kairosdb.KairosDbMetric;
import de.qaware.chronix.timeseries.MetricTimeSeries;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A format parser for the KairosDB procotol.
 * <p>
 * See https://kairosdb.github.io/docs/build/html/PushingData.html
 */
public class KairosDbFormatParser implements FormatParser {
    /**
     * UTF-8 charset. Used for decoding the given input stream.
     */
    private static final Charset UTF_8 = Charset.forName("utf-8");

    @Override
    public Iterable<MetricTimeSeries> parse(InputStream stream) throws FormatParseException {
        Map<Metric, MetricTimeSeries.Builder> metrics = new HashMap<>();

        List<KairosDbMetric> kairosMetrics = parseJson(stream);

        for (KairosDbMetric kairosMetric : kairosMetrics) {
            // If the metric is already known, add a point. Otherwise create the metric and add the point.
            Metric metric = new Metric(kairosMetric.getName(), kairosMetric.getTags());
            MetricTimeSeries.Builder metricBuilder = metrics.get(metric);
            if (metricBuilder == null) {
                metricBuilder = new MetricTimeSeries.Builder(kairosMetric.getName());
                for (Map.Entry<String, String> tagEntry : kairosMetric.getTags().entrySet()) {
                    metricBuilder.attribute(tagEntry.getKey(), tagEntry.getValue());
                }
                metrics.put(metric, metricBuilder);
            }

            Instant timestamp = convertTimestamp(kairosMetric.getTimestamp());
            metricBuilder.point(timestamp.toEpochMilli(), kairosMetric.getValue());
        }

        return metrics.values().stream().map(MetricTimeSeries.Builder::build).collect(Collectors.toList());
    }

    /**
     * Parses the given stream into a list of {@link KairosDbMetric}s.
     * <p>
     *
     * @param stream Stream.
     * @return Parsed metrics.
     */
    private List<KairosDbMetric> parseJson(InputStream stream) {
        Gson gson = new Gson();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, UTF_8));
        return gson.fromJson(reader, new TypeToken<List<KairosDbMetric>>() {
        }.getType());
    }

    /**
     * Converts the timestamp to an Instant.
     *
     * @param timestamp Timestamp.
     * @return Instant.
     */
    private Instant convertTimestamp(long timestamp) {
        return Instant.ofEpochMilli(timestamp);
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
