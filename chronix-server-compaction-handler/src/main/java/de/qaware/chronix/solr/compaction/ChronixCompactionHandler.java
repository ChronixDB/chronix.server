/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
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
 * The Chronix ingestion handler
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
