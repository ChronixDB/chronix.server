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

import org.apache.commons.io.IOUtils;
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
 * The Chronix ingestion handler
 *
 * @author f.lautenschlager
 */
public class ChronixIngestionHandler extends RequestHandlerBase implements SolrCoreAware, PluginInfoInitialized {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChronixIngestionHandler.class);

    @Override
    public void init(PluginInfo info) {

    }

    @Override
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        //Check: https://cwiki.apache.org/confluence/display/solr/Uploading+Data+with+Index+Handlers
        //TODO: Handle the request
        LOGGER.info("I was called");
        String result = IOUtils.toString(req.getContentStreams().iterator().next().getStream(), "UTF-8");
        LOGGER.info("Content is: {}", result);
    }

    @Override
    public String getDescription() {
        return "The Chronix ingestion handler.";
    }

    @Override
    public void inform(SolrCore core) {

    }
}
