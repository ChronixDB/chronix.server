/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.ingestion;

import de.qaware.chronix.converter.BinaryTimeSeries;
import de.qaware.chronix.converter.KassiopeiaSimpleConverter;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.io.IOUtils;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.PluginInfo;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.util.plugin.PluginInfoInitialized;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

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
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        //Check: https://cwiki.apache.org/confluence/display/solr/Uploading+Data+with+Index+Handlers
        //TODO: Handle the request

        //1) Parse input: Graphite line protocol, opentsdb, influxdb

        LOGGER.info("I was called");
        String result = IOUtils.toString(req.getContentStreams().iterator().next().getStream(), "UTF-8");
        LOGGER.info("Content is: {}", result);

        //2) Converter to time series
        MetricTimeSeries ts = new MetricTimeSeries.Builder("cpu_load_short")
                .attribute("host", "server02")
                .attribute("region", "us-west")
                .point(Instant.now().toEpochMilli(), 2061d)
                .build();

        KassiopeiaSimpleConverter converter = new KassiopeiaSimpleConverter();
        BinaryTimeSeries tsB = converter.to(ts);

        SolrInputDocument doc = new SolrInputDocument();

        tsB.getFields().forEach(doc::addField);

        //3) add to solr
        //generate uuid
        req.getCore();

    }

    @Override
    public String getDescription() {
        return "The Chronix ingestion handler.";
    }

    @Override
    public void inform(SolrCore core) {

    }
}
