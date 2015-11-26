/*
 * Copyright (C) 2015 QAware GmbH
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
package de.qaware.chronix.solr.range.query

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
                  "host:laptop OR end:47859 AND start:4578965 AND metric:\\Load\\avg",
                  "end:2015-11-25T12:06:57.330Z OR start:2015-12-25T12:00:00.000Z",
                  "end:NOW/DAY"]
        expected << ["", "metric:\\Load\\start",
                     "-start:[47858 TO *] AND -end:[* TO 4578964]",
                     "host:laptop OR -start:[47858 TO *] AND -end:[* TO 4578964] AND metric:\\Load\\avg",
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
