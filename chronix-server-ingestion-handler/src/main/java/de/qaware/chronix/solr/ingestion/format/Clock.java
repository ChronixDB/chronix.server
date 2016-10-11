package de.qaware.chronix.solr.ingestion.format;

import java.time.Instant;

/**
 * Provides the current time.
 */
public interface Clock {
    /**
     * Returns the current time.
     *
     * @return The current time.
     */
    Instant now();
}
