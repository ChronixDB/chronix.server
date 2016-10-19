/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.date

import org.apache.solr.util.DateMathParser
import spock.lang.Specification

import java.text.ParseException
/**
 * Unit test for the date query parser
 * @author f.lautenschlager
 */
class DateQueryParserTest extends Specification {

    def "test get numeric query terms"() {
        given:
        String[] dateFields = ["start:", "end:"]
        def dateQueryParser = new DateQueryParser(dateFields)

        when:
        def result = dateQueryParser.getNumericQueryTerms("start:1 AND end:0");

        then:
        result.length == 2
        result[0] == 1l
        result[1] == 0l

    }

    def "test replaceRangeQueryTerms"() {
        given:

        String[] dateFields = ["start:", "end:"]
        def dateQueryParser = new DateQueryParser(dateFields)

        when:
        def modifiedQuery = dateQueryParser.replaceRangeQueryTerms(query)

        then:
        modifiedQuery == expected

        where:
        query << ["", "metric:\\Load\\start",
                  "end:47859 AND start:4578965",
                  "host:laptop OR end:47859 AND start:4578965 AND metric:\\Load\\AVG",
                  "end:2015-11-25T12:06:57.330Z OR start:2015-12-25T12:00:00.000Z",
                  "end:NOW/DAY"]
        expected << ["", "metric:\\Load\\start",
                     "-start:[47858 TO *] AND -end:[* TO 4578964]",
                     "host:laptop OR -start:[47858 TO *] AND -end:[* TO 4578964] AND metric:\\Load\\AVG",
                     "-start:[1448453217329 TO *] OR -end:[* TO 1451044799999]",
                     "-start:[${testDateMathHelper("NOW/DAY") - 1} TO *]"]
    }

    def "testDateMathHelper"(String term) {
        String dateTerm = term.replace("NOW", "+0MILLISECOND");
        return new DateMathParser().parseMath(dateTerm).getTime();
    }

    def "test replace range query with invalid arguments"() {
        given:

        String[] dateFields = ["start:", "end:"]
        def dateQueryParser = new DateQueryParser(dateFields)

        when:
        dateQueryParser.replaceRangeQueryTerms(query)

        then:
        thrown ParseException.class

        where:
        query << ["start:hallo AND end:stop"]
    }

}
