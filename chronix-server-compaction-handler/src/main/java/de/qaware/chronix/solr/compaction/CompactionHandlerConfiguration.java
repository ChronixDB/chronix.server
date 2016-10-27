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
package de.qaware.chronix.solr.compaction;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

/**
 * Configures the compaction handler.
 * The primary use case is to facilitate testing.
 *
 * @author alex.christ
 */
public class CompactionHandlerConfiguration {

    /**
     * @param req the request
     * @param rsp the response
     * @return the solr update service
     */
    public SolrUpdateService solrUpdateService(SolrQueryRequest req, SolrQueryResponse rsp) {
        return new SolrUpdateService(req, rsp);
    }

    /**
     * @return the document loader
     */
    public LazyDocumentLoader documentLoader() {
        return new LazyDocumentLoader();
    }

    /**
     * @return the compactor
     */
    public LazyCompactor compactor() {
        return new LazyCompactor();
    }
}