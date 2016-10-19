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
