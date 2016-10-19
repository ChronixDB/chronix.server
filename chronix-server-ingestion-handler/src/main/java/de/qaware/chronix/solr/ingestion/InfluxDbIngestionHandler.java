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

import de.qaware.chronix.solr.ingestion.format.InfluxDbFormatParser;
import de.qaware.chronix.solr.ingestion.format.RealTimeClock;

/**
 * Handler to ingest the InfluxDB format.
 */
public class InfluxDbIngestionHandler extends AbstractIngestionHandler {
    public InfluxDbIngestionHandler() {
        super(new InfluxDbFormatParser(new RealTimeClock()));
    }

    @Override
    public String getDescription() {
        return "The Chronix InfluxDb ingestion handler.";
    }
}
