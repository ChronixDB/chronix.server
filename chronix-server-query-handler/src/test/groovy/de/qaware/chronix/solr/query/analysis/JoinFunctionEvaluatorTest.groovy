/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis

import org.apache.solr.common.SolrDocument
import spock.lang.Specification

/**
 * Unit test for the join function evaluator
 * @author f.lautenschlager
 */
class JoinFunctionEvaluatorTest extends Specification {
    def "test join function"() {
        given:
        def doc = new SolrDocument()
        doc.addField("host", "laptop")
        doc.addField("source", "groovy")
        doc.addField("metric", "unitTest")


        when:
        def joinFunction = JoinFunctionEvaluator.joinFunction(filterQueries)
        def joinKey = joinFunction.apply(doc)
        then:
        joinKey == result

        where:
        filterQueries << [validJoinFilterQuery(), noJoinFilterQuery(), noFilterQueries(), null]
        result << ["laptop-unitTest-groovy", "unitTest", "unitTest", "unitTest"]
    }

    private static String[] noFilterQueries() {
        []
    }

    private static String[] noJoinFilterQuery() {
        ["ag=max"]
    }

    private static String[] validJoinFilterQuery() {
        ["ag=max", "join=host,metric,source"]
    }

    def "test private constructor"() {
        when:
        JoinFunctionEvaluator.newInstance()
        then:
        noExceptionThrown()
    }
}
