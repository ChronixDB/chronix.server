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
     * Used to determine if a query contains an aggregation
     */
    public static final String FUNCTION_PARAM = "function=";

    /**
     * The function: aggregation or analysis
     */
    public static final String FUNCTION = "function";

    /**
     * The aggregation arguments
     */
    public static final String FUNCTION_ARGUMENTS = "function_arguments";

    /**
     * Used to join documents into one time series
     */
    public static final String JOIN_PARAM = "join=";

    /**
     * The resulting join key
     */
    public static final String JOIN_KEY = "join_key";

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

    /**
     * The solr version field. We remove that field in the function result
     */
    public static final String SOLR_VERSION_FIELD = "_version_";

    public static final String DATA_AS_JSON = "dataAsJson";

    private ChronixQueryParams() {
        //avoid instances
    }

}
