/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis.functions.aggregations;

import de.qaware.chronix.solr.query.analysis.functions.ChronixAggregation;
import de.qaware.chronix.solr.query.analysis.functions.FunctionType;
import de.qaware.chronix.solr.query.analysis.functions.FunctionValueMap;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;

/**
 * Integral aggregation
 *
 * @author f.lautenschlager
 */
public final class Integral implements ChronixAggregation<MetricTimeSeries> {

    /**
     * Calculates the integral of the given time series using the simpson integrator of commons math lib
     *
     * @param timeSeries       the time series as argument for the chronix function
     * @param functionValueMap the analysis and values result map
     */
    @Override
    public void execute(MetricTimeSeries timeSeries, FunctionValueMap functionValueMap) {

        if (timeSeries.isEmpty()) {
            functionValueMap.add(this, Double.NaN);
            return;
        }

        SimpsonIntegrator simpsonIntegrator = new SimpsonIntegrator();
        double integral = simpsonIntegrator.integrate(Integer.MAX_VALUE, x -> timeSeries.getValue((int) x), 0, timeSeries.size() - 1);

        functionValueMap.add(this, integral);
    }

    @Override
    public FunctionType getType() {
        return FunctionType.INTEGRAL;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        return new EqualsBuilder()
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .toHashCode();
    }
}
