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
