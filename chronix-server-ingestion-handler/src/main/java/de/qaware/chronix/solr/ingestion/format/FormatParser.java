package de.qaware.chronix.solr.ingestion.format;

import de.qaware.chronix.timeseries.MetricTimeSeries;

import java.io.InputStream;

/**
 * Parses data from an input stream to metric time series.
 */
public interface FormatParser {
    /**
     * Parses the given input stream to metric time series.
     *
     * @param stream Input stream.
     * @return Metric time series.
     * @throws FormatParseException If something went wrong while parsing the format.
     */
    Iterable<MetricTimeSeries> parse(InputStream stream) throws FormatParseException;
}
