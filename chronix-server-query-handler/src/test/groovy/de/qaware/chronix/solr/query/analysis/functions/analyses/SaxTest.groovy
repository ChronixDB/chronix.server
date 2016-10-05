/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis.functions.analyses

import de.qaware.chronix.solr.query.analysis.FunctionValueMap
import de.qaware.chronix.solr.query.analysis.functions.FunctionType
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit test for the SAX analysis
 * @author f.lautenschlager
 */
class SaxTest extends Specification {

    @Unroll
    def "test execute sax. Regex #regex expected result is #matches"() {
        given:
        def ts = new MetricTimeSeries.Builder("Sax")
        10.times {
            ts.point(it + 1, it + 1)
        }
        def analysisResult = new FunctionValueMap(1, 1, 1);

        def sax = new Sax(regex, 10, 10, 0.01)
        when:
        sax.execute(ts.build(), analysisResult)
        then:
        analysisResult.getAnalysisValue(0) == matches
        where:

        regex << ["*af*", "*ab*", "*ij*"]
        matches << [false, true, true]

    }

    def "test execute sax with invalid arguments"() {
        given:
        def ts = new MetricTimeSeries.Builder("Sax").build()
        def analysisResult = new FunctionValueMap(1, 1, 1);
        when:
        sax.execute(ts, analysisResult)
        then:
        !analysisResult.getAnalysisValue(0)

        where:
        sax << [new Sax("*af*", 7, 7, 0.01), new Sax("*af*", 7, 22, 0.01)]
    }

    def "test getArguments"() {
        expect:
        Arrays.equals(new Sax("*af*", 7, 7, 0.01).getArguments(),
                ["pattern=.*af.*", "paaSize=7", "alphabetSize=7", "threshold=0.01"] as String[])

    }

    def "test getType"() {
        expect:
        new Sax("*af*", 7, 7, 0.01).getType() == FunctionType.SAX
    }

    def "test needSubquery"() {
        expect:
        !new Sax("*af*", 7, 7, 0.01).needSubquery()
    }

    def "test getSubquery"() {
        expect:
        new Sax("*af*", 7, 7, 0.01).getSubquery() == null
    }
}
