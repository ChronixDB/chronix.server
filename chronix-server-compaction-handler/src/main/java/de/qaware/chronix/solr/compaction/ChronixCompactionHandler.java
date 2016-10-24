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

import org.apache.solr.core.PluginInfo;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.util.plugin.PluginInfoInitialized;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Chronix compaction handler
 *
 * @author f.lautenschlager
 */
public class ChronixCompactionHandler extends RequestHandlerBase implements SolrCoreAware, PluginInfoInitialized {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChronixCompactionHandler.class);

    @Override
    public void init(PluginInfo info) {

    }

    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        //Idea: We can call the handler for statistics (last compaction run, etc.)
        //The compaction runs timed or on a hard trigger
        LOGGER.info("I was called");
    }

    @Override
    public String getDescription() {
        return "The Chronix compaction handler.";
    }

    @Override
    public void inform(SolrCore core) {

    }
}
