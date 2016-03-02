/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr

import de.qaware.chronix.ChronixClient
import de.qaware.chronix.converter.KassiopeiaSimpleConverter
import de.qaware.chronix.solr.client.ChronixSolrStorage
import de.qaware.chronix.timeseries.MetricTimeSeries
import de.qaware.chronix.timeseries.dt.DoubleList
import de.qaware.chronix.timeseries.dt.LongList
import net.seninp.jmotif.sax.SAXException
import net.seninp.jmotif.sax.SAXProcessor
import net.seninp.jmotif.sax.alphabet.NormalAlphabet
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.text.DecimalFormat
import java.util.function.BinaryOperator
import java.util.function.Function
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
    SolrClient solr
    @Shared
    ChronixClient<MetricTimeSeries, SolrClient, SolrQuery> chronix

    @Shared
    def listStringField = ["List first part", "List second part"]
    @Shared
    def listIntField = [1I, 2I]
    @Shared
    def listLongField = [11L, 25L]
    @Shared
    def listDoubleField = [1.5D, 2.6D]


    @Shared
    Function<MetricTimeSeries, String> groupBy = new Function<MetricTimeSeries, String>() {
        @Override
        String apply(MetricTimeSeries ts) {
            StringBuilder metricKey = new StringBuilder();

            metricKey.append(ts.attribute("host")).append("-")
                    .append(ts.attribute("source")).append("-")
                    .append(ts.attribute("group")).append("-")
                    .append(ts.getMetric());

            return metricKey.toString();
        }
    }

    @Shared
    BinaryOperator<MetricTimeSeries> reduce = new BinaryOperator<MetricTimeSeries>() {
        @Override
        MetricTimeSeries apply(MetricTimeSeries t1, MetricTimeSeries t2) {
            MetricTimeSeries.Builder reduced = new MetricTimeSeries.Builder(t1.getMetric())
                    .points(concat(t1.getTimestamps(), t2.getTimestamps()),
                    concat(t1.getValues(), t2.getValues()))
                    .attributes(t1.attributes());
            return reduced.build();
        }
    }

    def DoubleList concat(DoubleList first, DoubleList second) {
        first.addAll(second)
        first
    }

    def LongList concat(LongList first, LongList second) {
        first.addAll(second)
        first
    }

    def setupSpec() {
        given:
        LOGGER.info("Setting up the integration test.")
        solr = new HttpSolrClient("http://localhost:8913/solr/chronix/")
        chronix = new ChronixClient(new KassiopeiaSimpleConverter<>(), new ChronixSolrStorage(200, groupBy, reduce))

        when: "We clean the index to ensure that no old data is loaded."
        sleep(30_000)
        solr.deleteByQuery("*:*")
        def result = solr.commit()

        and: "We add new data"

        LOGGER.info("Adding data to Chronix.")
        importTimeSeriesData();
        //we do a hart commit - only for testing purposes
        def updateResponse = solr.commit(true, true)
        LOGGER.info("Update Response of Commit is {}", updateResponse)

        then:
        result.status == 0

    }

    def importTimeSeriesData() {
        def url = ChronixClientTestIT.getResource("/timeSeries");
        def tsDir = new File(url.toURI())

        tsDir.listFiles().each { File file ->
            LOGGER.info("Processing file {}", file)
            def documents = new HashMap<Integer, MetricTimeSeries.Builder>()

            def attributes = file.name.split("_")
            def onlyOnce = true
            def nf = DecimalFormat.getInstance(Locale.ENGLISH);

            def filePoints = 0

            file.splitEachLine(";") { fields ->
                //Its the first line of a csv file
                if ("Date" == fields[0]) {
                    if (onlyOnce) {
                        fields.subList(1, fields.size()).eachWithIndex { String field, int i ->
                            def ts = new MetricTimeSeries.Builder(field)
                                    .attribute("host", attributes[0])
                                    .attribute("source", attributes[1])
                                    .attribute("group", attributes[2])

                            //Add some generic fields an values
                                    .attribute("myIntField", 5I)
                                    .attribute("myLongField", 8L)
                                    .attribute("myDoubleField", 5.5D)
                                    .attribute("myByteField", "String as byte".getBytes("UTF-8"))
                                    .attribute("myStringList", listStringField)
                                    .attribute("myIntList", listIntField)
                                    .attribute("myLongList", listLongField)
                                    .attribute("myDoubleList", listDoubleField)
                            documents.put(i, ts)

                        }
                    }
                } else {
                    //First field is the timestamp: 26.08.2013 00:00:17.361
                    def date = Date.parse("dd.MM.yyyy HH:mm:ss.SSS", fields[0])
                    fields.subList(1, fields.size()).eachWithIndex { String value, int i ->
                        documents.get(i).point(date.getTime(), nf.parse(value).doubleValue())
                        filePoints = i
                    }
                }
                onlyOnce = false
            }
            def buildedTimeSeries = new ArrayList<MetricTimeSeries>()
            documents.each { doc ->
                def saxString = sax(doc.value.metricTimeSeries.values.toArray())
                doc.value.attribute("sax", saxString)
                buildedTimeSeries.add(doc.value.build())
            }

            chronix.add(buildedTimeSeries, solr)
            def updateResponse = solr.commit(true, true)
            LOGGER.info("Update Response of Commit is {}", updateResponse)
        }
    }

    def sax(double[] values) {
        def sp = new SAXProcessor();
        def na = new NormalAlphabet();
        try {
            return String.valueOf(sp.ts2string(values, 9, na.getCuts(7), 0.01));
        } catch (SAXException e) {
            LOGGER.error("Could not convert time series to sax representation. Returning default", e);
        }
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
        selectedTimeSeries.attribute("myByteField") == "String as byte".getBytes("UTF-8")
        selectedTimeSeries.attribute("myStringList") == listStringField
        selectedTimeSeries.attribute("myIntList") == listIntField
        selectedTimeSeries.attribute("myLongList") == listLongField
        selectedTimeSeries.attribute("myDoubleList") == listDoubleField
    }

    @Unroll
    def "Test analysis query #analysisQuery"() {
        when:
        def query = new SolrQuery("metric:\\\\Load\\\\avg")
        query.addFilterQuery(analysisQuery)
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())
        then:
        timeSeries.size() == 1
        def selectedTimeSeries = timeSeries.get(0)

        selectedTimeSeries.size() >= points
        selectedTimeSeries.attribute("myIntField") == 5
        selectedTimeSeries.attribute("myLongField") == 8L
        selectedTimeSeries.attribute("myDoubleField") == 5.5D
        selectedTimeSeries.attribute("myByteField") == "String as byte".getBytes("UTF-8")
        selectedTimeSeries.attribute("myStringList") == listStringField
        selectedTimeSeries.attribute("myIntList") == listIntField
        selectedTimeSeries.attribute("myLongList") == listLongField
        selectedTimeSeries.attribute("myDoubleList") == listDoubleField

        where:
        analysisQuery << ["ag=max", "ag=min", "ag=avg", "ag=p:0.25", "ag=dev",
                          "analysis=trend", "analysis=outlier", "analysis=frequency:10,1",
                          "analysis=fastdtw:(metric:*Load*max),5,0.8", "analysis=sax:*,9,7,0.01"]
        points << [1, 1, 1, 1, 1, 7000, 7000, 7000, 7000, 7000]
    }

    def "Test analysis fastdtw"() {
        when:
        def query = new SolrQuery("metric:*Load*min")
        query.addFilterQuery("analysis=fastdtw:(metric:*Load*max),5,0.8")
        query.setFields("metric")
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())
        then:
        timeSeries.size() == 1
        def selectedTimeSeries = timeSeries.get(0)

        selectedTimeSeries.size()
        selectedTimeSeries.getMetric() == "\\Load\\max"
        selectedTimeSeries.attribute("analysis") == "FASTDTW"
        selectedTimeSeries.attribute("joinKey") == "\\Load\\max"
        selectedTimeSeries.attribute("value") == 0.056865779428449705
        selectedTimeSeries.attribute("analysisParam") == ["search radius=5", "max warping cost=0.8", "distance function=EUCLIDEAN"]
    }

    def "Test analysis sax"() {
        when:
        def query = new SolrQuery("metric:\\\\Tasks\\\\total")
        query.addFilterQuery("analysis=sax:*df*,9,7,0.01")
        query.setFields("metric")
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())
        then:
        timeSeries.size() == 1
        def selectedTimeSeries = timeSeries.get(0)

        selectedTimeSeries.size()
        selectedTimeSeries.getMetric() == "\\Tasks\\total"
        selectedTimeSeries.attribute("analysis") == "SAX"
        selectedTimeSeries.attribute("joinKey") == "\\Tasks\\total"
        selectedTimeSeries.attribute("value") == 1
        selectedTimeSeries.attribute("analysisParam") == ["pattern = .*df.*", "paaSize = 9", "alphabetSize = 7", "threshold = 0.01"]
    }

    def "Test query raw time series"() {
        when:
        def query = new SolrQuery("*:*")
        query.addField("dataAsJson:[dataAsJson],myIntField,myLongField,myDoubleField,myByteField,myStringList,myIntList,myLongList,myDoubleList")
        //query all documents
        List<MetricTimeSeries> timeSeries = chronix.stream(solr, query).collect(Collectors.toList())

        then:
        timeSeries.size() == 26i
        def selectedTimeSeries = timeSeries.get(0)

        selectedTimeSeries.size() >= 7000
        selectedTimeSeries.attribute("myIntField") == 5
        selectedTimeSeries.attribute("myLongField") == 8L
        selectedTimeSeries.attribute("myDoubleField") == 5.5D
        selectedTimeSeries.attribute("myByteField") == "String as byte".getBytes("UTF-8")
        selectedTimeSeries.attribute("myStringList") == listStringField
        selectedTimeSeries.attribute("myIntList") == listIntField
        selectedTimeSeries.attribute("myLongList") == listLongField
        selectedTimeSeries.attribute("myDoubleList") == listDoubleField
    }
}
