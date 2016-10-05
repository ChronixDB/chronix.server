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
package de.qaware.chronix.solr;

import com.google.common.collect.Lists;
import de.qaware.chronix.ChronixClient;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple csv reader for test purposes
 *
 * @author f.lautenschlager
 */
public final class CSVImporter {

    public static final List<String> LIST_STRING_FIELD = Lists.newArrayList("List first part", "List second part");
    public static final List<Integer> LIST_INT_FIELD = Lists.newArrayList(1, 2);
    public static final List<Long> LIST_LONG_FIELD = Lists.newArrayList(11L, 25L);
    public static final List<Double> LIST_DOUBLE_FIELD = Lists.newArrayList(1.5D, 2.6D);
    public static final byte[] BYTES = "String as byte".getBytes();
    private static final Logger LOGGER = LoggerFactory.getLogger(CSVImporter.class);


    private CSVImporter() {
        //avoid instances
    }

    /***
     * /**
     * Reads csv data from the resources dir 'timeSeries' and imports them to Chronix.
     *
     * @param chronix the chronix client
     * @param solr    the solr connection
     * @throws URISyntaxException if the file could not converted to a uri
     * @throws IOException        if the time series could not added to solr
     */
    public static void readAndImportCSV(ChronixClient<MetricTimeSeries, SolrClient, SolrQuery> chronix, HttpSolrClient solr) throws URISyntaxException, IOException {
        URL url = CSVImporter.class.getResource("/timeSeries");

        File tsDir = new File(url.toURI());
        File[] files = tsDir.listFiles();

        if (files == null) {
            LOGGER.warn("No files found. Returning");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS", Locale.GERMAN);

        for (File file : files) {
            LOGGER.info("Processing file {}", file);
            HashMap<Integer, MetricTimeSeries> documents = new HashMap<>();

            String[] attributes = file.getName().split("_");
            NumberFormat nf = DecimalFormat.getInstance(Locale.ENGLISH);

            AtomicInteger filePoints = new AtomicInteger(0);
            final AtomicBoolean onlyOnce = new AtomicBoolean(true);

            Files.lines(file.toPath()).forEach(line -> {
                String[] fields = line.split(";");

                //Its the first line of a csv file
                if ("Date".equals(fields[0])) {
                    if (onlyOnce.get()) {

                        for (int j = 1; j < fields.length; j++) {
                            MetricTimeSeries ts = new MetricTimeSeries.Builder(fields[j])
                                    .attribute("host", attributes[0])
                                    .attribute("source", attributes[1])
                                    .attribute("group", attributes[2])

                                    //Add some generic fields an values
                                    .attribute("myIntField", 5)
                                    .attribute("myLongField", 8L)
                                    .attribute("myDoubleField", 5.5D)
                                    .attribute("myByteField", BYTES)
                                    .attribute("myStringList", LIST_STRING_FIELD)
                                    .attribute("myIntList", LIST_INT_FIELD)
                                    .attribute("myLongList", LIST_LONG_FIELD)
                                    .attribute("myDoubleList", LIST_DOUBLE_FIELD)
                                    .build();
                            documents.put(j, ts);

                        }
                    }
                } else {
                    //First field is the timestamp: 26.08.2013 00:00:17.361
                    try {
                        long date = sdf.parse(fields[0]).getTime();

                        for (int j = 1; j < fields.length; j++) {
                            documents.get(j).add(date, nf.parse(fields[j]).doubleValue());
                        }
                        filePoints.addAndGet(fields.length);

                    } catch (ParseException e) {
                        LOGGER.error("Could not parse date: " + fields[0]);
                    }

                }
                onlyOnce.set(false);
            });
            chronix.add(documents.values(), solr);
        }
    }
}
