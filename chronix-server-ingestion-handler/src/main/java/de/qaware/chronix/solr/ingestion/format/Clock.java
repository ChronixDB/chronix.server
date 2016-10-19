/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.ingestion.format;

import java.time.Instant;

/**
 * Provides the current time.
 */
public interface Clock {
    /**
     * Returns the current time.
     *
     * @return The current time.
     */
    Instant now();
}
