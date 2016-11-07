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

import de.qaware.chronix.solr.ingestion.format.KairosDbFormatParser;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

/**
 * Handler to ingest the KairosDB format.
 */
public class KairosDbIngestionHandler extends AbstractIngestionHandler {
    public KairosDbIngestionHandler() {
        super(new KairosDbFormatParser());
    }

    @Override
    public String getDescription() {
        return "The Chronix KairosDB ingestion handler.";
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Override
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        super.handleRequestBody(req, rsp);

        // Add empty 'errors' field, otherwise the KairosDB client crashes.
        rsp.add("errors", new String[0]);
    }
}
