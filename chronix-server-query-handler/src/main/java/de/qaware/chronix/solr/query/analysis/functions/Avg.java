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
 * @author f.lautenschlager
 */
public final class Avg implements ChronixAnalysis {

    /**
     * Calculates the average value of the first time series.
     *
     * @param args the time series
     * @return the average or 0 if the list is empty
     */
    @Override
    public double execute(MetricTimeSeries... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Max aggregation needs at least one time series");
        }

        MetricTimeSeries timeSeries = args[0];

        double current = 0;
        for (int i = 0; i < timeSeries.size(); i++) {
            current += timeSeries.getValue(i);
        }

        return current / timeSeries.size();
    }

    @Override
    public String[] getArguments() {
        return new String[0];
    }

    @Override
    public AnalysisType getType() {
        return AnalysisType.AVG;
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
