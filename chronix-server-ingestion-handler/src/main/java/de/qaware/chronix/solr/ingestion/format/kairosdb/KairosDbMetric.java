package de.qaware.chronix.solr.ingestion.format.kairosdb;

import java.util.Map;

/**
 * A DTO of a KairosDB metric. Used for GSON deserializing.
 */
public class KairosDbMetric {
    private String name;
    private long timestamp;
    private double value;
    private Map<String, String> tags;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
}
