/*
 * Copyright (C) 2018 QAware GmbH
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
