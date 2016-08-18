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
 * Class to create join function on solr documents
 *
 * @author f.lautenschlager
 */
public final class JoinFunction implements Function<SolrDocument, String> {

    private String[] involvedFields;

    /**
     * The method checks if the filter queries contains a join filter query (join=field1,field2,field3).
     * If not, it returns a function with a default join key that uses the metric field.
     * Otherwise it uses the defined fields to build a join key field1-field2-field-3.
     *
     * @param filterQueries - the solr filter queries
     * @return a function to get a unique join key
     */
    public JoinFunction(String[] filterQueries) {
        if (filterQueries == null || filterQueries.length == 0) {
            involvedFields = new String[]{ChronixQueryParams.DEFAULT_JOIN_FIELD};
        } else {
            for (String filterQuery : filterQueries) {
                if (filterQuery.startsWith(ChronixQueryParams.JOIN_PARAM)) {
                    involvedFields = fields(filterQuery);
                    break;
                }
            }
        }
        if (involvedFields == null) {
            involvedFields = new String[]{ChronixQueryParams.DEFAULT_JOIN_FIELD};
        }
    }


    @Override
    public String apply(SolrDocument doc) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < involvedFields.length; i++) {
            String field = involvedFields[i];
            sb.append(doc.get(field.trim()));
            if (i < involvedFields.length - 1) {
                sb.append('-');
            }
        }
        return sb.toString();
    }


    /**
     * Validates if the given join function is (==) the default join function
     *
     * @param joinFunction the join function given by the callee
     * @return true if it is the same as the default join function (default = join on metric field)
     */
    public static boolean isDefaultJoinFunction(JoinFunction joinFunction) {
        return joinFunction.involvedFields.length == 1 && joinFunction.involvedFields[0].equals(ChronixQueryParams.DEFAULT_JOIN_FIELD);
    }

    /**
     * Returns the involved fields of the join function
     *
     * @return the involved fields for this join
     */
    public String[] involvedFields() {
        return involvedFields;
    }

    private static String[] fields(String filterQuery) {
        int startIndex = filterQuery.indexOf('=') + 1;
        String stringFields = filterQuery.substring(startIndex);
        return stringFields.split(ChronixQueryParams.JOIN_SEPARATOR);
    }


}
