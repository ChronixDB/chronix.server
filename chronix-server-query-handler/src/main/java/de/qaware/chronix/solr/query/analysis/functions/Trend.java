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

import de.qaware.chronix.solr.query.analysis.functions.math.LinearRegression;
import de.qaware.chronix.timeseries.MetricTimeSeries;

/**
 * The trend analysis
 *
 * @author f.lautenschlager
 */
public final class Trend implements ChronixAnalysis {
    /**
     * Detects trends in time series using a linear regression.
     *
     * @param args the time series
     * @return 1 if there is a positive trend, otherwise -1
     */
    @Override
    public double execute(MetricTimeSeries... args) {
        if (args.length <= 0) {
            throw new IllegalArgumentException("Trend detection needs at least one time series");
        }

        MetricTimeSeries timeSeries = args[0];
        timeSeries.sort();

        LinearRegression linearRegression = new LinearRegression(timeSeries.getTimestamps(), timeSeries.getValues());
        double slope = linearRegression.slope();
        return slope > 0 ? 1 : -1;
    }

    @Override
    public String[] getArguments() {
        return new String[0];
    }

    @Override
    public AnalysisType getType() {
        return AnalysisType.TREND;
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
