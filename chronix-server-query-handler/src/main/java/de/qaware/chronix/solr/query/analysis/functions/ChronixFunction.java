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

import de.qaware.chronix.solr.query.analysis.FunctionValueMap;

/**
 * The generic Chronix function interface
 *
 * @param <T> the type of the time series
 */
public interface ChronixFunction<T> {
    /**
     * Executes a Chronix function on the given time series. The result should be added to the function value map.
     *
     * @param timeSeries       the time series as argument for the chronix function
     * @param functionValueMap the analysis and values result map
     */
    void execute(T timeSeries, FunctionValueMap functionValueMap);

    /**
     * Gets the arguments of the function. Default is an empty string array.
     *
     * @return the arguments
     */
    default String[] getArguments() {
        return new String[0];
    }

    /**
     * @return the type of the analysis
     */
    FunctionType getType();
}
