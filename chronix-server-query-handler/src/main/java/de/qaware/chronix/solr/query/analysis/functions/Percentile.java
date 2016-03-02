/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis.functions;

import de.qaware.chronix.timeseries.MetricTimeSeries;

/**
 * Percentile aggregation analysis
 *
 * @author f.lautenschlager
 */
public final class Percentile implements ChronixAnalysis {


    private final double percentile;

    /**
     * Constructs a percentile aggregation
     *
     * @param percentile the percentile [0.0 ... 1.0]
     */
    public Percentile(double percentile) {
        this.percentile = percentile;
    }


    /**
     * Calculates the percentile of the first time series.
     *
     * @param args the time series
     * @return the percentile or 0 if the list is empty
     */
    @Override
    public double execute(MetricTimeSeries... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Percentile aggregation needs at least one time series");
        }

        MetricTimeSeries timeSeries = args[0];
        return de.qaware.chronix.solr.query.analysis.functions.math.Percentile.evaluate(timeSeries.getValues(), percentile);
    }

    @Override
    public String[] getArguments() {
        return new String[]{"percentile=" + percentile};
    }

    @Override
    public AnalysisType getType() {
        return AnalysisType.P;
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
