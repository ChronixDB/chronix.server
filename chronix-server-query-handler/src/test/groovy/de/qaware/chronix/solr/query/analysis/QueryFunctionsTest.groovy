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

import de.qaware.chronix.solr.query.analysis.functions.aggregations.Max
import de.qaware.chronix.solr.query.analysis.functions.analyses.Trend
import de.qaware.chronix.solr.query.analysis.functions.transformation.Vectorization
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

/**
 * Unit test for the query functions
 * @author f.lautenschlager
 */
class QueryFunctionsTest extends Specification {

    def "test query functions"() {
        given:
        def queryFunctions = new QueryFunctions<MetricTimeSeries>();
        def vectorization = new Vectorization(0.01f)

        when:
        queryFunctions.addAggregation(new Max())
        queryFunctions.addAnalysis(new Trend())
        queryFunctions.addTransformation(vectorization)

        then:
        !queryFunctions.isEmpty()
        queryFunctions.size() == 3

        queryFunctions.sizeOfAggregations() == 1
        queryFunctions.sizeOfAnalyses() == 1
        queryFunctions.sizeOfTransformations() == 1

        queryFunctions.getAggregations().contains(new Max())
        queryFunctions.getTransformations().contains(vectorization)
        queryFunctions.getAnalyses().contains(new Trend())

        queryFunctions.containsAggregations()
        queryFunctions.containsAnalyses()
        queryFunctions.containsTransformations()
    }

    def "test empty query functions"() {
        when:
        def queryFunctions = new QueryFunctions<MetricTimeSeries>();

        then:
        queryFunctions.isEmpty()
        queryFunctions.size() == 0

        queryFunctions.sizeOfAggregations() == 0
        queryFunctions.sizeOfAnalyses() == 0
        queryFunctions.sizeOfTransformations() == 0

        !queryFunctions.getAggregations().contains(new Max())
        !queryFunctions.getTransformations().contains(new Vectorization(0.01f))
        !queryFunctions.getAnalyses().contains(new Trend())

        !queryFunctions.containsAggregations()
        !queryFunctions.containsAnalyses()
        !queryFunctions.containsTransformations()
    }

}
