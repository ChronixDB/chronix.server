/*
 * Copyright (C) 2018 QAware GmbH
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
import de.qaware.chronix.converter.MetricTimeSeriesConverter
import de.qaware.chronix.converter.common.Compression
import de.qaware.chronix.solr.client.ChronixSolrStorage
import de.qaware.chronix.solr.query.ChronixQueryParams
import de.qaware.chronix.solr.util.CSVImporter
import de.qaware.chronix.solr.util.ChronixTestFunctions
import de.qaware.chronix.timeseries.MetricTimeSeries
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Ignore
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ChronixClientTestIT.class)

    //Test subjects
    @Shared
    HttpSolrClient solr
    @Shared
    ChronixClient<MetricTimeSeries, SolrClient, SolrQuery> chronix
    @Shared
    String solrBaseUrl = "http://localhost:8913/solr/chronix/"

    def setupSpec() {
        given:
        LOGGER.info("Setting up the integration test.")
        solr = new HttpSolrClient.Builder(solrBaseUrl).build()
        chronix = new ChronixClient(
                new MetricTimeSeriesConverter<>(),
                new ChronixSolrStorage(200, ChronixTestFunctions.GROUP_BY, ChronixTestFunctions.REDUCE))

        when: "We clean the index to ensure that no old data is loaded."
        sleep(10_000)
        solr.deleteByQuery("*:*")
        def result = solr.commit(true, true)

        and: "We add new data"
        LOGGER.info("Adding data to Chronix.")
        CSVImporter.readAndImportCSV(chronix, solr);
        //we do a hart commit - only for testing purposes
        def updateResponse = solr.commit(true, true)
        LOGGER.info("Update Response of Commit is {}", updateResponse)

        then:
        result.status == 0

    }

    def "Query all time series from Chronix"() {
        when:
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, new SolrQuery("*:*")).collect(Collectors.toList());

        then:
        timeSeries.size() == 26i
    }

    def "Query one specific time series (Swap\\free) from Chronix"() {
        when:
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, new SolrQuery("name:\\\\Swap\\\\free")).collect(Collectors.toList());

        then:
        timeSeries.size() == 1i
        def swapFree = timeSeries.get(0)
        swapFree.sort()

        swapFree.getName() == "\\Swap\\free"
        swapFree.getStart() == Instant.parse("2013-08-26T00:00:17.361Z").toEpochMilli()
        swapFree.getEnd() == Instant.parse("2013-09-01T23:59:18.096Z").toEpochMilli()
        swapFree.size() == 9693
        swapFree.getType() == "metric"

        //TODO: remove the version attribute (then 12)
        swapFree.attributes().size() == 13
        testAttributes(swapFree)

    }

    @Unroll
    def "Aggregation query #aggregation"() {
        when:
        def query = new SolrQuery("name:\\\\Load\\\\avg")

        def aggregationQuery = "metric{$aggregation}"
        if (withArgs != null) {
            aggregationQuery = "metric{$aggregation:$withArgs}"
        }

        query.setParam(ChronixQueryParams.CHRONIX_FUNCTION, aggregationQuery)
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())
        then:
        timeSeries.size() == 1
        def selectedTimeSeries = timeSeries.get(0)

        selectedTimeSeries.size() <= 0
        testAttributes(selectedTimeSeries)

        selectedTimeSeries.attribute("0_function_${aggregation}") as double == hasResult

        where:
        aggregation | withArgs | hasResult
        "max"       | null     | 5.47
        "min"       | null     | 0.04
        "avg"       | null     | 0.5883163107397165
        "p"         | 0.25     | 0.16
        "dev"       | null     | 0.7073607542900465
        "sum"       | null     | 5702.550000000072
        "count"     | null     | 9693
        "diff"      | null     | 0.6699999999999999
        "sdiff"     | null     | 0.6699999999999999
        "first"     | null     | 0.17
        "last"      | null     | 0.84
        "range"     | null     | 5.43
        "integral"  | null     | 5701.709725985112

    }

    @Unroll
    def "Analysis query: metric{#analysis:#withArgs}"() {
        when:
        def query = new SolrQuery("name:\\\\Load\\\\avg")

        def analysisQuery = "metric{$analysis}"
        if (withArgs != null) {
            analysisQuery = "metric{$analysis:$withArgs}"
        }

        query.setParam(ChronixQueryParams.CHRONIX_FUNCTION, analysisQuery)
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())

        then:
        timeSeries.size() == 1

        timeSeries.get(0).size() <= 0
        timeSeries.get(0).attribute("0_function_${analysis}") as boolean == hasResult
        testAttributes(timeSeries.get(0))

        where:

        analysis    | withArgs | hasResult
        "trend"     | null     | true
        "outlier"   | null     | true
        "frequency" | "10,5"   | false

    }

    @Unroll
    def "Transformation query: metric{#transformation:#withArgs}"() {

        when:
        def query = new SolrQuery("name:\\\\Tasks\\\\running")
        query.setParam(ChronixQueryParams.CHRONIX_FUNCTION, "metric{$transformation:$withArgs}")
        query.setFields("data")

        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())

        then:
        timeSeries.size() == 1
        timeSeries.get(0).size() == points
        def functionAttribute = timeSeries.get(0).attribute("0_function_${transformation}") as List<String>
        functionAttribute.contains(attributeValue)

        where:
        transformation | withArgs    | attributeValue   | points
        "top"          | 10          | "value=10"       | 10
        "bottom"       | 10          | "value=10"       | 10
        "scale"        | 4           | "value=4.0"      | 9693
        "divide"       | 4           | "value=4.0"      | 9693
        "sub"          | 4           | "value=4.0"      | 9693
        "timeshift"    | "10,DAYS"   | "amount=10"      | 9693
        "vector"       | 0.01        | "tolerance=0.01" | 7074
        "smovavg"      | 5           | "samples=5"      | 9689
        "movavg"       | "4,MINUTES" | "timeSpan=4"     | 9690

    }

    @Unroll
    def "Transformation query without args: metric{#transformation}"() {
        when:
        def query = new SolrQuery("name:\\\\Tasks\\\\running")
        def transformationQuery = "metric{$transformation}"
        query.setParam(ChronixQueryParams.CHRONIX_FUNCTION, transformationQuery)
        query.setFields("data")
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())

        then:
        timeSeries.size() == 1
        timeSeries.get(0).size() == points
        timeSeries.get(0).attributes().containsKey("0_function_${transformation}" as String)

        where:
        transformation | points
        "derivative"   | 9691
        "nnderivative" | 7302
        "distinct"     | 15
        "noop"         | 9693
    }

    @Ignore
    def "Analysis fastdtw"() {
        when:
        def query = new SolrQuery("name:*Load*min")
        query.setParam(ChronixQueryParams.CHRONIX_FUNCTION, "metric{fastdtw:(name:*Load*max),5,0.8}")
        query.setFields("name", "data")
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())
        then:
        timeSeries.size() == 1
        def selectedTimeSeries = timeSeries.get(0)

        selectedTimeSeries.size()
        selectedTimeSeries.getName() == "\\Load\\min"
        selectedTimeSeries.attribute("join_key") == "\\Load\\min-metric"
        selectedTimeSeries.attribute("0_function_fastdtw_\\Load\\max") == true
        selectedTimeSeries.attribute("0_function_arguments_fastdtw_\\Load\\max") == ["search radius=5", "max warping cost=0.8", "distance function=EUCLIDEAN"]

    }

    def "Function query with data as json"() {
        when:
        def query = new SolrQuery("name:\\\\Cpu\\\\sy")
        query.setParam(ChronixQueryParams.CHRONIX_FUNCTION, "metric{vector:0.1}")
        query.setFields("dataAsJson")
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())
        then:
        timeSeries.size() == 1
        timeSeries.get(0).size() == 2
    }

    def "Function query with dataAsJson and join"() {
        when:
        def query = new SolrQuery("name:\\\\Cpu*")
        query.setParam(ChronixQueryParams.CHRONIX_JOIN, "dynamic_s,name")
        query.setFields("dataAsJson")
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())

        then:
        timeSeries.size() == 8
        def ts = timeSeries.get(0)
        ts.size() == 9693
        def joinKey = ts.attribute("dynamic_s")[0] + "-" + ts.getName()
        timeSeries.get(0).attribute("join_key") == joinKey
    }

    def "Join documents with data: #data on dynamic field"() {
        when:
        def query = new SolrQuery("name:\\\\Cpu*")
        query.setParam(ChronixQueryParams.CHRONIX_JOIN, "dynamic_s")
        query.setFields(data)

        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())

        then:
        timeSeries.size() == 1
        timeSeries.get(0).attributes().size() == attributeSize
        timeSeries.get(0).size() == 77544

        where:
        data    | attributeSize
        "data"  | 2
        "+data" | 13

    }

    @Unroll
    def "Join documents with data: #data"() {
        when:
        def query = new SolrQuery("name:\\\\Cpu*")
        query.setParam(ChronixQueryParams.CHRONIX_JOIN, "group")
        query.setFields(data)

        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())

        then:
        timeSeries.size() == 1
        timeSeries.get(0).attributes().size() == attributeSize
        timeSeries.get(0).size() == 77544

        where:
        data    | attributeSize
        "data"  | 2
        "+data" | 12
    }


    def "Query raw time series"() {
        when:
        def query = new SolrQuery("*:*")
        query.addField("dataAsJson,myIntField,myLongField,myDoubleField,myByteField,myStringList,myIntList,myLongList,myDoubleList")
        //query all documents
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())

        then:
        timeSeries.size() == 26i
        def selectedTimeSeries = timeSeries.get(0)

        selectedTimeSeries.size() == 9693
        testAttributes(selectedTimeSeries)
    }

    def "Query raw time series with +dataAsJson"() {
        when:
        def query = new SolrQuery("*:*")
        query.addField("+dataAsJson")
        //query all documents
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())

        then:
        timeSeries.size() == 26i
        def selectedTimeSeries = timeSeries.get(0)

        selectedTimeSeries.size() == 9693
        selectedTimeSeries.attributes().size() == 13
    }

    def "Query with compression result"() {
        when:
        def query = new SolrQuery("*:*")
        //Enable server side compression
        HttpSolrClient test_solr = new HttpSolrClient.Builder(solrBaseUrl)
                .allowCompression(true).build()
        //query all documents
        List<MetricTimeSeries> timeSeries = chronix.stream(test_solr, query).collect(Collectors.toList())

        then:
        timeSeries.size() == 26i
        for (int i = 0; i < timeSeries.size(); i++) {
            timeSeries.get(i).size() == 9693
            timeSeries.get(i).attributes().size() == 13
        }

    }


    def "Raw query with compression activated"() {
        when:
        def connection = (solrBaseUrl + "select?indent=on&q=*:*&wt=json").toURL().openConnection()
        connection.setRequestProperty("Accept-Encoding", "gzip")
        def result = Compression.decompress(connection.getInputStream().bytes)

        then:
        result.length != 0
    }

    def "Test all aggregations are executed 50 times (ticket #153,#155)"() {
        given:
        def query = new SolrQuery("*:*")
        query.setParam(ChronixQueryParams.CHRONIX_FUNCTION, "metric{avg;max}")
        query.addField("-data")

        def success = true
        def iteration = 50
        when:
        while (success && iteration > 0) {
            List<MetricTimeSeries> timeSeriesList = chronix.stream(solr, query).collect(Collectors.toList())
            iteration = iteration - 1;

            for (MetricTimeSeries timeSeries in timeSeriesList) {
                def avg = timeSeries.attribute("0_function_avg")
                if (avg == null) {
                    avg = timeSeries.attribute("1_function_avg")
                }
                def max = timeSeries.attribute("1_function_max")
                if (max == null) {
                    max = timeSeries.attribute("0_function_max")
                }

                if (max == null || avg == null) {
                    success = false
                }
            }

            //ensure we got 26 every time
            if (timeSeriesList.size() != 26i) {
                success = false
            }
        }


        then:
        success

    }

    def testAttributes(MetricTimeSeries series) {
        series.attribute("myIntField") as Set<Integer> == [5] as Set<Integer>
        series.attribute("myLongField") as Set<Long> == [8L] as Set<Long>
        series.attribute("myDoubleField") as Set<Double> == [5.5D] as Set<Double>
        (series.attribute("myByteField") as List).size() == 7
        series.attribute("myStringList") == CSVImporter.LIST_STRING_FIELD
        series.attribute("myIntList") == CSVImporter.LIST_INT_FIELD
        series.attribute("myLongList") == CSVImporter.LIST_LONG_FIELD
        series.attribute("myDoubleList") == CSVImporter.LIST_DOUBLE_FIELD
    }
}
