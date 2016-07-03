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
package de.qaware.chronix.solr.query.analysis;

import de.qaware.chronix.solr.query.ChronixQueryParams;
import org.apache.solr.common.SolrDocument;

import java.util.function.Function;

/**
 * Class to create join function on lucene documents
 *
 * @author f.lautenschlager
 */
public final class JoinFunctionEvaluator {

    private static final Function<SolrDocument, String> DEFAULT_JOIN_FUNCTION = doc -> doc.getFieldValue(ChronixQueryParams.DEFAULT_JOIN_FIELD).toString();


    private JoinFunctionEvaluator() {
        //avoid instances
    }

    /**
     * The method checks if the filter queries contains a join filter query (join=field1,field2,field3).
     * If not, it returns a function with a default join key that uses the metric field.
     * Otherwise it uses the defined fields to build a join key field1-field2-field-3.
     *
     * @param filterQueries - the solr filter queries
     * @return a function to get a unique join key
     */
    public static Function<SolrDocument, String> joinFunction(String[] filterQueries) {
        if (filterQueries == null || filterQueries.length == 0) {
            return DEFAULT_JOIN_FUNCTION;
        }

        for (String filterQuery : filterQueries) {
            if (filterQuery.startsWith(ChronixQueryParams.JOIN_PARAM)) {
                final String[] fields = fields(filterQuery);
                return doc -> joinKey(fields, doc);
            }
        }

        return DEFAULT_JOIN_FUNCTION;
    }

    /**
     * Validates if the given join function is (==) the default join function
     *
     * @param joinFunction the join function given by the callee
     * @return true if it is the same as the default join function (default = join on metric field)
     */
    public static boolean isDefaultJoinFunction(Function<SolrDocument, String> joinFunction) {
        return DEFAULT_JOIN_FUNCTION == joinFunction;
    }

    private static String[] fields(String filterQuery) {
        int startIndex = filterQuery.indexOf('=') + 1;
        String stringFields = filterQuery.substring(startIndex);
        return stringFields.split(ChronixQueryParams.JOIN_SEPARATOR);
    }

    private static String joinKey(String[] fields, SolrDocument doc) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            sb.append(doc.get(field.trim()));

            if (i < fields.length - 1) {
                sb.append('-');
            }
        }
        return sb.toString();
    }
}
