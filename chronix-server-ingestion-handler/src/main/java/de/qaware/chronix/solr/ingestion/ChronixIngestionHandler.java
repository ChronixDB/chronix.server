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
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        //Check: https://cwiki.apache.org/confluence/display/solr/Uploading+Data+with+Index+Handlers
        //TODO: Handle the request

        //1) Parse input: Graphite line protocol, opentsdb, influxdb

        LOGGER.info("I was called");
        String result = IOUtils.toString(req.getContentStreams().iterator().next().getStream(), "UTF-8");
        LOGGER.info("Content is: {}", result);

        //2) Converter to time series
        MetricTimeSeries ts = new MetricTimeSeries.Builder("cpu_load_short")
                .attribute("host","server02")
                .attribute("region","us-west")
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
