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
 * The standard deviation analysis
 *
 * @author f.lautenschlager
 */
public final class StdDev implements ChronixAnalysis {
    /**
     * Calculates the standard deviation of the first time series.
     *
     * @param args the time series
     * @return the percentile or 0 if the list is empty
     */
    @Override
    public double execute(MetricTimeSeries... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Standard deviation aggregation needs at least one time series");
        }
        MetricTimeSeries timeSeries = args[0];

        return de.qaware.chronix.solr.query.analysis.functions.math.StdDev.dev(timeSeries.getValues());
    }

    @Override
    public String[] getArguments() {
        return new String[0];
    }

    @Override
    public AnalysisType getType() {
        return AnalysisType.DEV;
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
