package de.qaware.chronix.integration

import de.qaware.chronix.ChronixClient
import de.qaware.chronix.converter.KassiopeiaConverter
import de.qaware.chronix.dts.Pair
import de.qaware.chronix.timeseries.TimeSeries
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer
import org.apache.solr.core.CoreContainer
import spock.lang.Specification

import java.time.Instant
import java.util.stream.Collectors

/**
 * Kassiopeia and Chronix integration test
 * @author f.lautenschlager
 */
class IntegrationTest extends Specification {

    def "test integration of kassiopeia and chronix client"() {
        given:

        def converter = new KassiopeiaConverter();
        def chronixClient = new ChronixClient<TimeSeries<Long, Double>>(converter);
        SolrClient solr = createAnEmbeddedSolrClient()

        //Delete the index
        solr.deleteByQuery("*:*")
        solr.commit()

        def now = Instant.now()

        when:
        def timeSeriesList = new ArrayList<TimeSeries<Long, Double>>()
        timeSeriesList.add(preFilledTimeSeries(now))
        chronixClient.add(timeSeriesList, solr);
        //hard commit to ensure data is indexed immediately
        solr.commit()

        then:
        //query the time series
        def result = chronixClient.stream(new SolrQuery("host:NB-Chronix-1"), now.toEpochMilli() - 1, now.plusMillis(1001).toEpochMilli(), solr).collect(Collectors.toList())
        result.size() == 1;

        //check the attrbiutes
        TimeSeries queriesTS = result.get(0)
        queriesTS.getAttribute("host") == "NB-Chronix-1"
        queriesTS.getAttribute("process") == "integrationTest"
        queriesTS.getAttribute("max") == 1000
        queriesTS.getAttribute("min") == 0
    }


    def createAnEmbeddedSolrClient() {
        System.setProperty("solr.data.dir", "${new File("build").absolutePath}/data");

        File solrXml = new File("src/test/resources/chronix/solr.xml")
        CoreContainer solrContainer = CoreContainer.createAndLoad(solrXml.getParent(), solrXml)
        return new EmbeddedSolrServer(solrContainer, "chronix")
    }

    def preFilledTimeSeries(Instant now) {
        def points = new ArrayList<Pair<Long, Double>>()
        for (int i = 0; i < 1000; i++) {
            Pair<Long, Double> pair = Pair.pairOf(now.plusMillis(i).toEpochMilli(), (double) i);
            points.add(pair);
        }

        //Create a time series and add some attributes
        def ts = new TimeSeries<>(points);
        ts.addAttribute("host", "NB-Chronix-1")
        ts.addAttribute("process", "integrationTest")
        ts.addAttribute("max", 1000)
        ts.addAttribute("min", 0)

        ts
    }


}