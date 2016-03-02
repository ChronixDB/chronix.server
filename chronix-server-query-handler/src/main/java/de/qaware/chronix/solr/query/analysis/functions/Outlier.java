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

import de.qaware.chronix.solr.query.analysis.functions.math.Percentile;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import de.qaware.chronix.timeseries.dt.DoubleList;

/**
 * The outlier analysis
 *
 * @author f.lautenschlager
 */
public class Outlier implements ChronixAnalysis {

    /**
     * Detects outliers using the default box plot implementation.
     * An outlier every value that is above (q3-q1)*1.5*q3 where qN is the nth percentile
     *
     * @param args the time series
     * @return 1 if there are outliers, otherwise -1
     */
    @Override
    public double execute(MetricTimeSeries... args) {
        if (args.length <= 0) {
            throw new IllegalArgumentException("Trend detection needs at least one time series");
        }

        MetricTimeSeries timeSeries = args[0];

        if (timeSeries.isEmpty()) {
            return -1;
        }

        DoubleList points = timeSeries.getValues();

        double q1 = Percentile.evaluate(points, .25);
        double q3 = Percentile.evaluate(points, .75);
        double threshold = (q3 - q1) * 1.5 + q3;
        for (int i = 0; i < points.size(); i++) {
            double point = points.get(i);
            if (point > threshold) {
                return 1;
            }
        }
        return -1;
    }

    @Override
    public String[] getArguments() {
        return new String[0];
    }

    @Override
    public AnalysisType getType() {
        return AnalysisType.OUTLIER;
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
