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

/**
 * The current implemented aggregations
 *
 * @author f.lautenschlager
 */
public enum AnalysisType {

    //Aggregations
    AVG,
    MIN,
    MAX,
    DEV,
    P,
    //Analysis
    TREND,
    OUTLIER,
    FREQUENCY,
    FASTDTW,
    SAX;

    /**
     * Checks if the given type is a high level analysis
     *
     * @param type - the analysis type
     * @return true if the analysis type is a high level analysis, otherwise false
     */
    public static boolean isHighLevel(AnalysisType type) {
        return TREND == type || OUTLIER == type || FREQUENCY == type || FASTDTW == type || SAX == type;
    }

    /**
     * Check if the given type is an isAggregation
     *
     * @param type - the analysis type
     * @return true if an isAggregation, otherwise false
     */
    public static boolean isAggregation(AnalysisType type) {
        return !isHighLevel(type);
    }


}
