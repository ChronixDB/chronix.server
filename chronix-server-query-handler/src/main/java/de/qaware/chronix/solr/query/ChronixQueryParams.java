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
package de.qaware.chronix.solr.query;

/**
 * The Chronix Query parameter constants
 *
 * @author f.lautenschlager
 */
public final class ChronixQueryParams {

    public static final String CHRONIX_FUNCTION = "cf";

    public static final String CHRONIX_JOIN = "cj";

    /**
     * The function: aggregation or analysis
     */
    public static final String FUNCTION = "function";

    /**
     * The aggregation arguments
     */
    public static final String FUNCTION_ARGUMENTS = FUNCTION + "_arguments";


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
    public static final String DEFAULT_JOIN_FIELD_1 = "metric";
    public static final String DEFAULT_JOIN_FIELD_2 = "type";

    /**
     * The character used to split fields in join filter query
     */
    public static final String JOIN_SEPARATOR = ",";

    public static final String DATA_AS_JSON = "dataAsJson";

    private ChronixQueryParams() {
        //avoid instances
    }
}
