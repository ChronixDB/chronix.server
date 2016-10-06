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

import de.qaware.chronix.converter.KassiopeiaSimpleConverter;
import de.qaware.chronix.solr.ingestion.format.FormatParser;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.PluginInfo;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.CommitUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorChain;
import org.apache.solr.util.plugin.PluginInfoInitialized;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Abstract class for every ingestion handler.
 * <p>
 * The concrete class only has to provide a suitable {@link FormatParser} instance.
 */
public abstract class AbstractIngestionHandler extends RequestHandlerBase implements SolrCoreAware, PluginInfoInitialized {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractIngestionHandler.class);

    private final FormatParser formatParser;

    /**
     * Constructor.
     *
     * @param formatParser Format parser.
     */
    public AbstractIngestionHandler(FormatParser formatParser) {
        this.formatParser = formatParser;
    }

    @Override
    public void init(PluginInfo info) {

    }

    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        if (req.getContentStreams() == null) {
            LOGGER.warn("no content stream");
            rsp.add("error", "No content stream");
            return;
        }

        InputStream stream = req.getContentStreams().iterator().next().getStream();

        KassiopeiaSimpleConverter converter = new KassiopeiaSimpleConverter();

        for (MetricTimeSeries series : formatParser.parse(stream)) {
            SolrInputDocument document = new SolrInputDocument();
            converter.to(series).getFields().forEach(document::addField);
            storeDocument(document, req, rsp);
        }
    }

    private void storeDocument(SolrInputDocument document, SolrQueryRequest req, SolrQueryResponse rsp) throws IOException {
        UpdateRequestProcessorChain processorChain = req.getCore().getUpdateProcessorChain(req.getParams());
        UpdateRequestProcessor processor = processorChain.createProcessor(req, rsp);
        AddUpdateCommand cmd = new AddUpdateCommand(req);
        cmd.solrDoc = document;
        processor.processAdd(cmd);
        processor.processCommit(new CommitUpdateCommand(req, false));
        processor.finish();
    }


    @Override
    public void inform(SolrCore core) {

    }
}
