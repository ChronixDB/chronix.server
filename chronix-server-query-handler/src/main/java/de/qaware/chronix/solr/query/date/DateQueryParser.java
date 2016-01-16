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
package de.qaware.chronix.solr.query.date;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.util.DateMathParser;

import java.text.ParseException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class is used to first, transform queries like start:NOW-30DAYS
 * in expressions like 'NOW as long + 30 Days as long' and second,
 * to build matching range queries on our time series documents.
 * The current queries are supported:
 * <p>
 * - end:47859 AND start:4578965
 * - end:2015-11-25T12:06:57.330Z OR start:2015-12-25T12:00:00.000Z
 * - start:NOW-30DAYS AND stop:NOW+30DAYS
 *
 * @author f.lautenschlager
 */
public class DateQueryParser {

    private final String[] dateFields;

    private final Pattern solrDateMathPattern;
    private final Pattern instantDatePattern;

    /**
     * Constructs a date query parser
     *
     * @param dateFields - the date fields
     */
    public DateQueryParser(String[] dateFields) {
        this.dateFields = dateFields;
        this.solrDateMathPattern = Pattern.compile(".*(NOW|DAY|MONTH|YEAR).*");
        this.instantDatePattern = Pattern.compile("\\d{4}\\-\\d{2}\\-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z");
    }

    /**
     * Converts the term for the date fields into an numeric representation.
     * [0] -> numeric value for date field [0]
     * [1] -> numeric value for date field [1]
     * <p>
     * If the query does not contain a date field the value is represented as -1.
     *
     * @param query - the user query
     * @return an array containing numeric representations of the date fields
     * @throws ParseException
     */
    public long[] getNumericQueryTerms(String query) throws ParseException {
        long[] result = new long[dateFields.length];
        for (int i = 0; i < dateFields.length; i++) {
            if (query.contains(dateFields[i])) {
                String dateField = dateFields[i];
                String dateTerm = getTokenTerm(query, dateField);
                result[i] = getNumberRepresentation(dateTerm);
            } else {
                result[i] = -1;
            }
        }

        return result;
    }

    /**
     * @param query - the plain user query
     * @return an enriched plain solr query
     * @throws ParseException if there are characters that can not be parsed
     */
    public String replaceRangeQueryTerms(String query) throws ParseException {
        Map<String, String> replacements = new HashMap<>();

        String queryWithPlaceholders = markQueryWithPlaceholders(query, replacements);
        return replacePlaceholders(queryWithPlaceholders, replacements);
    }

    /**
     * Converts the given date term into a numeric representation
     *
     * @param dateTerm - the date term, e.g, start:NOW+30DAYS
     * @return the long representation of the date term
     * @throws ParseException if the date term could not be evaluated
     */
    private long getNumberRepresentation(String dateTerm) throws ParseException {
        long numberRepresentation;
        if (StringUtils.isNumeric(dateTerm)) {
            numberRepresentation = Long.valueOf(dateTerm);
        } else if (solrDateMathPattern.matcher(dateTerm).matches()) {
            numberRepresentation = parseDateTerm(dateTerm);
        } else if (instantDatePattern.matcher(dateTerm).matches()) {
            numberRepresentation = Instant.parse(dateTerm).toEpochMilli();
        } else {
            throw new ParseException("Could not parse date representation '" + dateTerm + "'", 0);
        }
        return numberRepresentation;
    }

    /**
     * Replaces the placeholders with concrete values
     *
     * @param query        - the query with placeholders
     * @param replacements - the replacements
     * @return a query with concrete values
     */
    private String replacePlaceholders(String query, Map<String, String> replacements) {
        String resultQuery = query;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            resultQuery = resultQuery.replace(entry.getKey(), entry.getValue());
        }
        return resultQuery;
    }

    /**
     * @param query        - the origin query
     * @param replacements - a map for to put in the replacements
     * @return a query with placeholders and the matching replacements
     * @throws ParseException if the date term could not be parsed
     */
    private String markQueryWithPlaceholders(String query, Map<String, String> replacements) throws ParseException {
        String placeHolderQuery = query;
        for (int i = 0; i < dateFields.length; i++) {
            String dateField = dateFields[i];

            if (placeHolderQuery.contains(dateField)) {
                String dateTerm = getTokenTerm(placeHolderQuery, dateField);
                long numberRepresentation = getNumberRepresentation(dateTerm);
                String rangeQuery = getDateRangeQuery(numberRepresentation, dateField);
                placeHolderQuery = placeHolderQuery.replace(dateField + dateTerm, keyPart(i));

                //add the placeholders
                replacements.put(keyPart(i), rangeQuery);
            }
        }
        return placeHolderQuery;
    }

    /**
     * Important: The end of an term is marked by an " "
     *
     * @param query      - the origin query
     * @param startToken - the start token
     * @return the term for the start token
     */
    private String getTokenTerm(String query, String startToken) {
        int tokenLength = startToken.length();
        int index = query.indexOf(startToken);
        int stopIndex = query.indexOf(' ', index);

        if (stopIndex > -1) {
            return query.substring(index + tokenLength, stopIndex);

        }

        return query.substring(index + tokenLength);
    }

    /**
     * @return the end date for the range query
     * @throws ParseException
     */
    private long parseDate(String dateQueryTerm) throws ParseException {
        return new DateMathParser().parseMath(dateQueryTerm).getTime();
    }

    /**
     * Builds a range query that
     *
     * @param value - the date value as long
     * @param field - the date field (start or end)
     * @return a solr range query
     */
    private String getDateRangeQuery(long value, String field) {

        if ("start:".equals(field)) {
            //We don`t need documents, that have and end before our start
            // q = -end[* TO (START-1)]
            return "-end:[* TO " + (value - 1) + "]";
        } else {
            //We don`t need documents, that have and start after our end
            // q = -start[* TO (START-1)]
            return "-start:[" + (value - 1) + " TO *]";
        }

    }


    private String keyPart(int i) {
        return "key-" + i;
    }

    /**
     * Parses a solr date to long representation
     *
     * @param term - the solr date term (NOW + 30 DAYS)
     * @return the term as long
     * @throws ParseException - if the term could not be parsed
     */
    private long parseDateTerm(String term) throws ParseException {
        String dateTerm = term.replace("NOW", "+0MILLISECOND");
        return parseDate(dateTerm);
    }
}
