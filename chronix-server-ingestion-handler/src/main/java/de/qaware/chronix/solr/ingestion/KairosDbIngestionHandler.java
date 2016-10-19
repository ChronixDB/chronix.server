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

    @Override
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        super.handleRequestBody(req, rsp);

        // Add empty 'errors' field, otherwise the KairosDB client crashes.
        rsp.add("errors", new String[0]);
    }
}
