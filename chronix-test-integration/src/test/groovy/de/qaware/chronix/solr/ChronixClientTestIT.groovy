/*
 *    Copyright (C) 2015 QAware GmbH
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
package de.qaware.chronix.solr

import de.qaware.chronix.ChronixClient
import de.qaware.chronix.converter.KassiopeiaSimpleConverter
import de.qaware.chronix.dts.MetricDataPoint
import de.qaware.chronix.solr.client.ChronixSolrStorage
import de.qaware.chronix.timeseries.MetricTimeSeries
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.HttpSolrClient
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant
import java.util.stream.Collectors

/**
 * Tests the integration of Chronix and an embedded solr.
 * Fields also have to be registered in the schema.xml
 * (\src\test\resources\de\qaware\chronix\chronix\conf\schema.xml)
 *
 * @author f.lautenschlager
 */
class ChronixClientTestIT extends Specification {

    //Test subjects
    @Shared
    SolrClient httpCoreClient
    @Shared
    ChronixClient chronix

    //Test constants
    @Shared
    long start
    @Shared
    long end
    @Shared
    def listStringField = ["List first part", "List second part"]
    @Shared
    def listIntField = [1I, 2I]
    @Shared
    def listLongField = [11L, 25L]
    @Shared
    def listDoubleField = [1.5D, 2.6D]

    def setupSpec() {
        given:
        httpCoreClient = new HttpSolrClient("http://localhost:8983/solr/chronix/")
        chronix = new ChronixClient(new KassiopeiaSimpleConverter(), new ChronixSolrStorage())
        start = Instant.now().toEpochMilli()
        end = start + (9 * 500)

        when: "We clean the index to ensure that no old data is loaded."
        sleep(10_000)
        httpCoreClient.deleteByQuery("*:*")
        def result = httpCoreClient.commit()

        and: "We add new data"

        //add some documents
        def documents = new ArrayList()
        10.times {
            //id is set by solr, we have no custom field registered in the solr schema.xml
            def builder = new MetricTimeSeries.Builder("Integration-Test-Metric-${it}")
                    .attribute("myIntField", 5)
                    .attribute("myLongField", 8L)
                    .attribute("myDoubleField", 5.5)
                    .attribute("myByteField", "String as byte".getBytes("UTF-8"))
                    .attribute("myStringList", listStringField)
                    .attribute("myIntList", listIntField)
                    .attribute("myLongList", listLongField)
                    .attribute("myDoubleList", listDoubleField)

            10.times {
                builder.point(new MetricDataPoint(start + (it * 500), it * 2))
            }
            //add an outlier
            builder.point(new MetricDataPoint(start + (it * 500), 9999))

            documents.add(builder.build())
        }

        chronix.add(documents, httpCoreClient)

        //we do a hart commit - only for testing purposes
        httpCoreClient.commit()

        then:
        result.status == 0
    }


    def "Test add and query time series to Chronix with Solr"() {
        when:
        //query all documents
        List<MetricTimeSeries> timeSeries = chronix.stream(httpCoreClient, new SolrQuery("*:*"), start, end, 200).collect(Collectors.toList())

        then:
        timeSeries.size() == 10i
        def selectedTimeSeries = timeSeries.get(0)

        selectedTimeSeries.start == start
        selectedTimeSeries.end == end
        selectedTimeSeries.points.size() == 11i
        selectedTimeSeries.attribute("myIntField") == 5
        selectedTimeSeries.attribute("myLongField") == 8L
        selectedTimeSeries.attribute("myDoubleField") == 5.5D
        selectedTimeSeries.attribute("myByteField") == "String as byte".getBytes("UTF-8")
        selectedTimeSeries.attribute("myStringList") == listStringField
        selectedTimeSeries.attribute("myIntList") == listIntField
        selectedTimeSeries.attribute("myLongList") == listLongField
        selectedTimeSeries.attribute("myDoubleList") == listDoubleField
    }

    @Unroll
    def "Test analysis query #analysisQuery"() {
        when:
        def query = new SolrQuery("*:*");
        query.addFilterQuery(analysisQuery)
        List<MetricTimeSeries> timeSeries = chronix.stream(httpCoreClient, query, start, end, 200).collect(Collectors.toList())
        then:
        timeSeries.size() == 10
        def selectedTimeSeries = timeSeries.get(0)

        selectedTimeSeries.size() == points
        selectedTimeSeries.attribute("myIntField") == 5
        selectedTimeSeries.attribute("myLongField") == 8L
        selectedTimeSeries.attribute("myDoubleField") == 5.5D
        selectedTimeSeries.attribute("myByteField") == "String as byte".getBytes("UTF-8")
        //selectedTimeSeries.attribute("myStringList") == listStringField
        //selectedTimeSeries.attribute("myIntList") == listIntField
        //selectedTimeSeries.attribute("myLongList") == listLongField
        //selectedTimeSeries.attribute("myDoubleList") == listDoubleField

        where:
        analysisQuery << ["ag=max", "ag=min", "ag=avg", "ag=p:0.25", "ag=dev", "analysis=trend", "analysis=outlier"]
        points << [1, 1, 1, 1, 1, 11, 11]

    }
}
