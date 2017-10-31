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
package de.qaware.chronix.solr.type.metric.functions.analyses;


import de.qaware.chronix.server.functions.ChronixAnalysis;
import de.qaware.chronix.server.functions.FunctionValueMap;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Chronix analysis wrapper for SAX (Symbolic Aggregate Approximation)
 * Calculates the sax representation and applies a given regex (solr syntax).
 *
 * @author f.lautenschlager
 */
public final class Sax implements ChronixAnalysis<MetricTimeSeries> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChronixAnalysis.class);

    private final Pattern pattern;
    private final SAXProcessor saxProcessor;
    private final int paaSize;
    private final int alphabetSize;
    private final double threshold;

    /**
     * Constructs a sax analysis
     *
     * @param args the arguments holding the regex (*ABC*, * is replaces with .*), the paaSize, the alphabetSize and the threshold.
     */
    public Sax(String[] args) {

        String regex = args[0];
        int paaSize = Integer.valueOf(args[1]);
        int alphabetSize = Integer.valueOf(args[2]);
        double threshold = Double.valueOf(args[3]);

        this.pattern = Pattern.compile(regex.replaceAll("\\*", ".*"));
        this.paaSize = paaSize;
        this.alphabetSize = alphabetSize;
        this.threshold = threshold;
        this.saxProcessor = new SAXProcessor();

    }

    /**
     * Applies SAX to the given time series and check if the given regex matches.
     *
     * @param timeSeries the time series
     * @return -1 if the sax of the given time series does not match the regex, otherwise 1
     */
    @Override
    public void execute(MetricTimeSeries timeSeries, FunctionValueMap functionValueMap) {
        try {
            double[] cuts = new NormalAlphabet().getCuts(alphabetSize);
            String sax = String.valueOf(saxProcessor.ts2string(timeSeries.getValuesAsArray(), paaSize, cuts, threshold));

            if (pattern.matcher(sax).matches()) {
                functionValueMap.add(this, true, null);
                return;
            }

        } catch (SAXException e) {
            LOGGER.error("Could not calculate sax representation.", e);
        }
        functionValueMap.add(this, false, null);
    }

    @Override
    public String[] getArguments() {
        return new String[]{"pattern=" + pattern.pattern(), "paaSize=" + paaSize, "alphabetSize=" + alphabetSize, "threshold=" + threshold};
    }

    @Override
    public String getQueryName() {
        return "sax";
    }

    @Override
    public String getTimeSeriesType() {
        return "metric";
    }
}