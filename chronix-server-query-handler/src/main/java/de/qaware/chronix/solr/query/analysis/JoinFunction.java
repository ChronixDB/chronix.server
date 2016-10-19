/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis;

import de.qaware.chronix.solr.query.ChronixQueryParams;
import org.apache.solr.common.SolrDocument;

import java.util.Arrays;
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

    /**
     * Validates if the given join function is (==) the default join function
     *
     * @param joinFunction the join function given by the callee
     * @return true if it is the same as the default join function (default = join on metric field)
     */
    public static boolean isDefaultJoinFunction(JoinFunction joinFunction) {
        return joinFunction.involvedFields.length == 1 && joinFunction.involvedFields[0].equals(ChronixQueryParams.DEFAULT_JOIN_FIELD);
    }

    private static String[] fields(String filterQuery) {
        int startIndex = filterQuery.indexOf('=') + 1;
        String stringFields = filterQuery.substring(startIndex);
        return stringFields.split(ChronixQueryParams.JOIN_SEPARATOR);
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
     * Returns the involved fields of the join function
     *
     * @return the involved fields for this join
     */
    public String[] involvedFields() {
        return Arrays.copyOf(involvedFields, involvedFields.length);
    }


}
