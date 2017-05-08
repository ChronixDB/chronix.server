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

import de.qaware.chronix.converter.MetricTimeSeriesConverter;
import de.qaware.chronix.solr.ingestion.format.FormatParser;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.CommitUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.zip.GZIPInputStream;

/**
 * Abstract class for every ingestion handler.
 * <p>
 * The concrete class only has to provide a suitable {@link FormatParser} instance.
 */
public abstract class AbstractIngestionHandler extends RequestHandlerBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractIngestionHandler.class);

/* added to handle gzipped data in */

    public static InputStream detectGzip(InputStream is) throws IOException {
        PushbackInputStream pb = new PushbackInputStream(is, 2);
        byte [] signature = new byte[2];
        pb.read(signature);
        pb.unread(signature);
        if (signature[0] == (byte) GZIPInputStream.GZIP_MAGIC && signature[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8)) {
          return new GZIPInputStream(pb);
        }   else {
          return pb;
        }
    }

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
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        formatResponseAsJson(req);

        if (req.getContentStreams() == null) {
            LOGGER.warn("no content stream");
            rsp.add("error", "No content stream");
            return;
        }

        InputStream stream = req.getContentStreams().iterator().next().getStream();
        stream = detectGzip(stream);

        MetricTimeSeriesConverter converter = new MetricTimeSeriesConverter();

        UpdateRequestProcessorChain processorChain = req.getCore().getUpdateProcessorChain(req.getParams());
        UpdateRequestProcessor processor = processorChain.createProcessor(req, rsp);
        try {
            for (MetricTimeSeries series : formatParser.parse(stream)) {
                SolrInputDocument document = new SolrInputDocument();
                converter.to(series).getFields().forEach(document::addField);
                storeDocument(document, processor, req);
            }

            LOGGER.debug("Committing transaction...");
            processor.processCommit(new CommitUpdateCommand(req, false));
            LOGGER.debug("Committed transaction");
        } finally {
            processor.finish();
        }
    }

    /**
     * Sets the response format to JSON.
     *
     * @param req Original Solr request.
     */
    private void formatResponseAsJson(SolrQueryRequest req) {
        // Return the result as JSON
        ModifiableSolrParams params = new ModifiableSolrParams(req.getParams());
        params.set("wt", "json");
        req.setParams(params);
    }

    private void storeDocument(SolrInputDocument document, UpdateRequestProcessor processor, SolrQueryRequest req) throws IOException {
        LOGGER.debug("Adding Solr document...");
        AddUpdateCommand cmd = new AddUpdateCommand(req);
        cmd.solrDoc = document;
        processor.processAdd(cmd);
        LOGGER.debug("Added Solr document");
    }
}
