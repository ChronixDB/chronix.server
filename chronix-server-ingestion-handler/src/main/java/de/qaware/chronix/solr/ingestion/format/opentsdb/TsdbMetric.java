package de.qaware.chronix.solr.ingestion.format.opentsdb;

import java.util.HashMap;

/**
 * A DTO of a TSDB metric. Uses for GSON deserializing.
 */
public class TsdbMetric {
    private String metric;
    private long timestamp;
    private double value;
    private HashMap<String, String> tags;

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public HashMap<String, String> getTags() {
        return tags;
    }

    public void setTags(HashMap<String, String> tags) {
        this.tags = tags;
    }
}
