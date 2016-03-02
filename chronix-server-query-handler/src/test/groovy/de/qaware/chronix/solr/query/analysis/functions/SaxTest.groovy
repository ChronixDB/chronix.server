/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis.functions

import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit test for the SAX analysis
 * @author f.lautenschlager
 */
class SaxTest extends Specification {

    @Unroll
    def "test execute sax with valid arguments. Regex #regex expected result is #matches"() {
        given:
        def ts = new MetricTimeSeries.Builder("Sax")
        10.times {
            ts.point(it + 1, it + 1)
        }


        def sax = new Sax(regex, 10, 10, 0.01)
        when:
        def result = sax.execute(ts.build())
        then:
        result == matches
        where:

        regex << ["*af*", "*ab*", "*ij*"]
        matches << [-1.0d, 1.0d, 1.0d]

    }

    def "test execute sax with invalid arguments"() {
        given:
        def ts = new MetricTimeSeries.Builder("Sax").build()
        when:
        def result = sax.execute(ts)
        then:
        result == -1.0d

        where:
        sax << [new Sax("*af*", 7, 7, 0.01), new Sax("*af*", 7, 22, 0.01)]
    }

    def "test sax analysis with null argument"() {
        when:
        new Sax("*af*", 7, 7, 0.01).execute(null)
        then:
        thrown IllegalArgumentException
    }


    def "test getArguments"() {
        expect:
        new Sax("*af*", 7, 7, 0.01).getArguments() == ["pattern = .*af.*", "paaSize = 7", "alphabetSize = 7", "threshold = 0.01"] as String[]

    }

    def "test getType"() {
        expect:
        new Sax("*af*", 7, 7, 0.01).getType() == AnalysisType.SAX
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
