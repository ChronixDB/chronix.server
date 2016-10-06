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

import java.io.IOException;
import java.io.InputStream;

/**
 * Abstract class for every ingestion handler.
 * <p>
 * The concrete class only has to provide a suitable {@link FormatParser} instance.
 */
public abstract class AbstractIngestionHandler extends RequestHandlerBase implements SolrCoreAware, PluginInfoInitialized {
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
