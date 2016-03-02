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

/***
 * A class holding the parsed analysis with its arguments
 *
 * @author f.lautenschlager
 */
public interface ChronixAnalysis {

    /**
     * Executes the analysis
     *
     * @param args the time series
     * @return the value of the analysis
     */
    double execute(MetricTimeSeries... args);

    /**
     * @return the arguments
     */
    String[] getArguments();

    /**
     * @return the type of the analysis
     */
    AnalysisType getType();

    /**
     * @return if the analysis needs a getSubquery
     */
    boolean needSubquery();

    /**
     * @return the getSubquery of the analysis
     */
    String getSubquery();
}
