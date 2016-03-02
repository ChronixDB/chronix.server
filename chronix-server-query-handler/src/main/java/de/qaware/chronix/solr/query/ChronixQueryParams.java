/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query;

/**
 * The Chronix Query parameter constants
 *
 * @author f.lautenschlager
 */
public final class ChronixQueryParams {

    /**
     * Used to determine if a query contains an isAggregation
     */
    public static final String AGGREGATION_PARAM = "ag=";

    /**
     * Used to determine if a query contains an analysis
     */
    public static final String ANALYSIS_PARAM = "analysis=";

    /**
     * Used to join documents into one time series
     */
    public static final String JOIN_PARAM = "join=";

    /**
     * The query start as long. Stored in the solr request params
     * after date range evaluation
     */
    public static final String QUERY_START_LONG = "query_start_long";
    /**
     * The query end as long. Stored in the solr request params
     * after date range evaluation
     */
    public static final String QUERY_END_LONG = "query_end_long";

    /**
     * The start field including ":"
     */
    public static final String DATE_START_FIELD = "start:";

    /**
     * The end field including ":"
     */
    public static final String DATE_END_FIELD = "end:";

    /**
     * The default join field
     */
    public static final String DEFAULT_JOIN_FIELD = "metric";

    /**
     * The character used to split fields in join filter query
     */
    public static final String JOIN_SEPARATOR = ",";

    private ChronixQueryParams() {
        //avoid instances
    }

}
