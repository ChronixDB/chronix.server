package de.qaware.chronix.solr.ingestion.format;

import java.time.Instant;

/**
 * A real time clock.
 */
public class RealTimeClock implements Clock {
    @Override
    public Instant now() {
        return Instant.now();
    }
}
