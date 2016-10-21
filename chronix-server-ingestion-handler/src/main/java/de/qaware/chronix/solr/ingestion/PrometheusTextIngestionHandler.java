/*
 * Copyright (C) 2016 QAware GmbH
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
package de.qaware.chronix.solr.ingestion;

import de.qaware.chronix.solr.ingestion.format.PrometheusTextFormatParser;
import de.qaware.chronix.solr.ingestion.format.RealTimeClock;

/**
 * Handler to ingest the Prometheus text format.
 */
public class PrometheusTextIngestionHandler extends AbstractIngestionHandler {
    public PrometheusTextIngestionHandler() {
        super(new PrometheusTextFormatParser(new RealTimeClock()));
    }

    @Override
    public String getDescription() {
        return "The Chronix Prometheus text format ingestion handler.";
    }
}
