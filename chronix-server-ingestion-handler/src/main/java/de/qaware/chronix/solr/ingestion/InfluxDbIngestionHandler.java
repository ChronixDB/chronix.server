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

import de.qaware.chronix.solr.ingestion.format.InfluxDbFormatParser;
import de.qaware.chronix.solr.ingestion.format.RealTimeClock;

/**
 * Handler to ingest the InfluxDB format.
 */
public class InfluxDbIngestionHandler extends AbstractIngestionHandler {
    /**
     * Constructor.
     */
    public InfluxDbIngestionHandler() {
        super(new InfluxDbFormatParser(new RealTimeClock()));
    }

    @Override
    public String getDescription() {
        return "The Chronix InfluxDb ingestion handler.";
    }
}
