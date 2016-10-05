/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis.functions.analyses;

import de.qaware.chronix.solr.query.analysis.FunctionValueMap;
import de.qaware.chronix.solr.query.analysis.functions.ChronixAnalysis;
import de.qaware.chronix.solr.query.analysis.functions.FunctionType;
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
     * @param regex the regex in solr syntax. (* is replaced with .*)
     */
    public Sax(String regex, int paaSize, int alphabetSize, double threshold) {
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
    public FunctionType getType() {
        return FunctionType.SAX;
    }

    @Override
    public boolean needSubquery() {
        return false;
    }

    @Override
    public String getSubquery() {
        return null;
    }
}
