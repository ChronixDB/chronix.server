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

import de.qaware.chronix.solr.ingestion.format.OpenTsdbTelnetFormatParser;

/**
 * Handler to ingest the OpenTSDB telnet format.
 */
public class OpenTsdbTelnetIngestionHandler extends AbstractIngestionHandler {
    public OpenTsdbTelnetIngestionHandler() {
        super(new OpenTsdbTelnetFormatParser());
    }

    @Override
    public String getDescription() {
        return "The Chronix OpenTSDB telnet ingestion handler.";
    }
}
