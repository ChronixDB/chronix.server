/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.retention;

/**
 * Constants for the retention query handler
 *
 * @author f.lautenschlager
 */
public final class RetentionConstants {

    public static final String QUERY_FIELD = "queryField";
    public static final String REMOVE_TIME_SERIES_OLDER = "timeSeriesAge";
    public static final String OPTIMIZE_AFTER_DELETION = "optimizeAfterDeletion";
    public static final String SOFT_COMMIT = "softCommit";
    public static final String REMOVE_DAILY_AT = "removeDailyAt";
    public static final String RETENTION_URL = "retentionUrl";

    /**
     * Private constructor to avoid instances
     */
    private RetentionConstants() {

    }
}
