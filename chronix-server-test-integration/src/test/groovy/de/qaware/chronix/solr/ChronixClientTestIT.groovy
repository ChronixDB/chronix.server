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
package de.qaware.chronix.solr

import de.qaware.chronix.ChronixClient
import de.qaware.chronix.converter.KassiopeiaSimpleConverter
import de.qaware.chronix.converter.common.Compression
import de.qaware.chronix.solr.client.ChronixSolrStorage
import de.qaware.chronix.timeseries.MetricTimeSeries
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.Collectors

/**
 * Tests the integration of Chronix and an embedded solr.
 * Fields also have to be registered in the schema.xml
 * (\src\test\resources\de\qaware\chronix\chronix\conf\schema.xml)
 *
 * @author f.lautenschlager
 */
class ChronixClientTestIT extends Specification {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChronixClientTestIT.class);

    //Test subjects
    @Shared
    HttpSolrClient solr
    @Shared
    ChronixClient<MetricTimeSeries, SolrClient, SolrQuery> chronix

    def setupSpec() {
        given:
        LOGGER.info("Setting up the integration test.")
        solr = new HttpSolrClient("http://localhost:8913/solr/chronix/")
        chronix = new ChronixClient(new KassiopeiaSimpleConverter<>(), new ChronixSolrStorage(200, ChronixTestFunctions.groupBy, ChronixTestFunctions.reduce))

        when: "We clean the index to ensure that no old data is loaded."
        sleep(30_000)
        solr.deleteByQuery("*:*")
        def result = solr.commit()

        and: "We add new data"

        LOGGER.info("Adding data to Chronix.")
        CSVImporter.readAndImportCSV(chronix, solr);
        //we do a hart commit - only for testing purposes
        def updateResponse = solr.commit(true, true)
        LOGGER.info("Update Response of Commit is {}", updateResponse)

        then:
        result.status == 0

    }

    def "Test add and query time series to Chronix with Solr"() {
        when:
        //query all documents
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, new SolrQuery("*:*")).collect(Collectors.toList());

        then:
        timeSeries.size() == 26i
        def selectedTimeSeries = timeSeries.get(0)

        selectedTimeSeries.size() >= 7000
        selectedTimeSeries.attribute("myIntField") == 5
        selectedTimeSeries.attribute("myLongField") == 8L
        selectedTimeSeries.attribute("myDoubleField") == 5.5D
        selectedTimeSeries.attribute("myByteField") == CSVImporter.BYTES
        selectedTimeSeries.attribute("myStringList") == CSVImporter.LIST_STRING_FIELD
        selectedTimeSeries.attribute("myIntList") == CSVImporter.LIST_INT_FIELD
        selectedTimeSeries.attribute("myLongList") == CSVImporter.LIST_LONG_FIELD
        selectedTimeSeries.attribute("myDoubleList") == CSVImporter.LIST_DOUBLE_FIELD
    }

    @Unroll
    def "Test aggregation query #analysisQuery"() {
        when:
        def query = new SolrQuery("metric:\\\\Load\\\\avg")
        query.addFilterQuery(analysisQuery)
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())
        then:
        timeSeries.size() == 1
        def selectedTimeSeries = timeSeries.get(0)

        selectedTimeSeries.size() <= 0
        selectedTimeSeries.attribute("myIntField") as Set<Integer> == [5] as Set<Integer>
        selectedTimeSeries.attribute("myLongField") as Set<Long> == [8L] as Set<Long>
        selectedTimeSeries.attribute("myDoubleField") as Set<Double> == [5.5D] as Set<Double>
        (selectedTimeSeries.attribute("myByteField") as List).size() == 7
        selectedTimeSeries.attribute("myStringList") == CSVImporter.LIST_STRING_FIELD
        selectedTimeSeries.attribute("myIntList") == CSVImporter.LIST_INT_FIELD
        selectedTimeSeries.attribute("myLongList") == CSVImporter.LIST_LONG_FIELD
        selectedTimeSeries.attribute("myDoubleList") == CSVImporter.LIST_DOUBLE_FIELD

        where:
        analysisQuery << ["function=max", "function=min", "function=avg", "function=p:0.25", "function=dev", "function=sum",
                          "function=count", "function=diff", "function=sdiff", "function=first", "function=last", "function=range",
                          "function=integral"
        ]
    }

    @Unroll
    def "Test analysis query #analysisQuery"() {
        when:
        def query = new SolrQuery("metric:\\\\Load\\\\avg")
        query.addFilterQuery(analysisQuery)
        query.setFields("+data")
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())
        then:
        timeSeries.size() == 1
        def selectedTimeSeries = timeSeries.get(0)

        selectedTimeSeries.size() >= points
        selectedTimeSeries.attribute("myIntField") as Set<Integer> == [5] as Set<Integer>
        selectedTimeSeries.attribute("myLongField") as Set<Long> == [8L] as Set<Long>
        selectedTimeSeries.attribute("myDoubleField") as Set<Double> == [5.5D] as Set<Double>
        (selectedTimeSeries.attribute("myByteField") as List).size() == 7
        selectedTimeSeries.attribute("myStringList") == CSVImporter.LIST_STRING_FIELD
        selectedTimeSeries.attribute("myIntList") == CSVImporter.LIST_INT_FIELD
        selectedTimeSeries.attribute("myLongList") == CSVImporter.LIST_LONG_FIELD
        selectedTimeSeries.attribute("myDoubleList") == CSVImporter.LIST_DOUBLE_FIELD

        where:
        analysisQuery << ["function=trend", "function=outlier", "function=frequency:10,1", "function=fastdtw:(metric:*Load*max),5,0.8"]
        points << [7000, 7000, 7000, 7000]
    }

    @Unroll
    def "test transformation query: #analysisQuery"() {
        when:
        def query = new SolrQuery("metric:\\\\Tasks\\\\running")
        query.addFilterQuery(analysisQuery)
        query.setFields("+data")
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())
        then:
        timeSeries.size() == 1
        def selectedTimeSeries = timeSeries.get(0)

        selectedTimeSeries.size() == points
        selectedTimeSeries.attribute(attributeKeys)[0] == attributeValues

        where:
        analysisQuery << ["function=vector:0.01", "function=scale:4", "function=divide:4", "function=movavg:4,minutes",
                          "function=top:10", "function=bottom:10", "function=add:4", "function=sub:4", "function=timeshift:10,DAYS"]
        attributeKeys << ["0_function_vector", "0_function_scale", "0_function_divide", "0_function_movavg",
                          "0_function_top", "0_function_bottom", "0_function_add", "0_function_sub", "0_function_timeshift"]
        attributeValues << ["tolerance=0.01", "value=4.0", "value=4.0", "timeSpan=4", "value=10", "value=10",
                            "value=4.0", "value=4.0",
                            "amount=10"]
        points << [7074, 9693, 9693, 9690, 10, 10, 9693, 9693, 9693]
    }

    @Unroll
    def "test transformation query #analysisQuery with empty arguments"() {
        when:
        def query = new SolrQuery("metric:\\\\Tasks\\\\running")
        query.addFilterQuery(analysisQuery)
        query.setFields("+data")
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())
        then:
        timeSeries.size() == 1
        def selectedTimeSeries = timeSeries.get(0)

        selectedTimeSeries.size() == points

        where:
        analysisQuery << ["function=derivative", "function=nnderivative", "function=distinct"]
        attributeKeys << ["0_function_derivative", "0_function_nnderivative", "0_function_distinct"]

        points << [9691, 7302, 15]
    }

    def "Test analysis fastdtw"() {
        when:
        def query = new SolrQuery("metric:*Load*min")
        query.addFilterQuery("function=fastdtw:(metric:*Load*max),5,0.8")
        query.setFields("metric", "data")
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())
        then:
        timeSeries.size() == 1
        def selectedTimeSeries = timeSeries.get(0)

        selectedTimeSeries.size()
        selectedTimeSeries.getMetric() == "\\Load\\min"
        selectedTimeSeries.attribute("join_key") == "\\Load\\min"
        selectedTimeSeries.attribute("0_function_fastdtw_\\Load\\max") == true
        selectedTimeSeries.attribute("0_function_arguments_fastdtw_\\Load\\max") == ["search radius=5", "max warping cost=0.8", "distance function=EUCLIDEAN"]

    }

    def "test function query with data as json"() {
        when:
        def query = new SolrQuery("metric:\\\\Cpu\\\\sy")
        query.addFilterQuery("function=vector:0.1")
        query.setFields("dataAsJson")
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())
        then:
        timeSeries.size() == 1
        timeSeries.get(0).size() == 2
    }

    def "test function query with dataAsJson and join"() {
        when:
        def query = new SolrQuery("metric:\\\\Cpu*")
        query.addFilterQuery("join=group")
        query.setFields("dataAsJson")
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())
        then:
        timeSeries.size() == 1
        timeSeries.get(0).size() == 77544
    }

    @Unroll
    def "test join documents with data: #ifData"() {
        when:
        def query = new SolrQuery("metric:\\\\Cpu*")
        query.addFilterQuery("join=group")
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())
        then:
        timeSeries.size() == 1
        if (ifData) {
            timeSeries.get(0).size() == 77544
        } else {
            timeSeries.get(0).size() == 0
        }

        where:
        data << ["", "+data"]
        ifData << [false, true]
    }

    def "test analysis with empty result"() {
        when:
        def query = new SolrQuery("metric:\\\\Load\\\\min")
        query.addFilterQuery("function=frequency:10,9")
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())
        then:
        timeSeries.size() == 0
    }


    def "Test query raw time series"() {
        when:
        def query = new SolrQuery("*:*")
        query.addField("dataAsJson,myIntField,myLongField,myDoubleField,myByteField,myStringList,myIntList,myLongList,myDoubleList")
        //query all documents
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())

        then:
        timeSeries.size() == 26i
        def selectedTimeSeries = timeSeries.get(0)

        selectedTimeSeries.size() >= 7000
        selectedTimeSeries.attribute("myIntField")[0] == 5
        selectedTimeSeries.attribute("myLongField")[0] == 8L
        selectedTimeSeries.attribute("myDoubleField")[0] == 5.5d
        selectedTimeSeries.attribute("myByteField")[0] == CSVImporter.BYTES
        selectedTimeSeries.attribute("myStringList") == CSVImporter.LIST_STRING_FIELD
        selectedTimeSeries.attribute("myIntList") == CSVImporter.LIST_INT_FIELD
        selectedTimeSeries.attribute("myLongList") == CSVImporter.LIST_LONG_FIELD
        selectedTimeSeries.attribute("myDoubleList") == CSVImporter.LIST_DOUBLE_FIELD
    }

    def "Test query raw time series with +dataAsJson"() {
        when:
        def query = new SolrQuery("*:*")
        query.addField("+dataAsJson")
        //query all documents
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())

        then:
        timeSeries.size() == 26i
        def selectedTimeSeries = timeSeries.get(0)

        selectedTimeSeries.size() >= 7000
        selectedTimeSeries.attributes().size() == 13
    }

    def "Test query with compression result"() {
        when:
        def query = new SolrQuery("*:*")
        //Enable serverside compression
        solr.setAllowCompression(true)
        //query all documents
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())

        then:
        timeSeries.size() == 26i
        def selectedTimeSeries = timeSeries.get(0)

        selectedTimeSeries.size() >= 7000
        selectedTimeSeries.attributes().size() == 12
    }

    @Unroll
    def "Test raw query with compression activated: #withCompression"() {
        when:
        def connection = "http://localhost:8913/solr/chronix/select?indent=on&q=*:*&wt=json".toURL().openConnection()
        connection.setRequestProperty("Accept-Encoding", "gzip");
        def result = Compression.decompress(connection.getInputStream().bytes)

        then:
        if (withCompression) {
            result.length > 0
        } else {
            result.length == 0
        }

        where:
        withCompression << [true, false]
    }
}
