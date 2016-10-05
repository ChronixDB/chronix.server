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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The current implemented AGGREGATIONS
 *
 * @author f.lautenschlager
 */
public enum FunctionType {

    //Aggregations
    AVG,
    MIN,
    MAX,
    DEV,
    P,
    SUM,
    COUNT,
    FIRST,
    LAST,
    RANGE,
    DIFF,
    SDIFF,
    INTEGRAL,
    //Pre-Aggregations
    PRE_AVG,
    PRE_MIN,
    PRE_MAX,
    PRE_SUM,
    PRE_COUNT,
    //Analysis
    TREND,
    OUTLIER,
    FREQUENCY,
    FASTDTW,
    SAX,
    //Transformations
    VECTOR,
    DIVIDE,
    SCALE,
    BOTTOM,
    TOP,
    MOVAVG,
    DERIVATIVE,
    NNDERIVATIVE,
    ADD,
    SUB,
    TIMESHIFT,
    DISTINCT,
    //Strace
    SPLIT,
    //LSOF
    GROUP;

    //Sets to hold the aggregations, analyses and transformations.
    //Otherwise the complexity of if(type == X || type == X ...) is to high
    private static final Set<FunctionType> AGGREGATIONS = new HashSet<>();
    private static final Set<FunctionType> ANALYSES = new HashSet<>();
    private static final Set<FunctionType> TRANSFORMATIONS = new HashSet<>();
    private static final Set<FunctionType> STRACE = new HashSet<>();
    private static final Set<FunctionType> LSOF = new HashSet<>();

    static {
        Collections.addAll(AGGREGATIONS, AVG, MIN, MAX, DEV, P, SUM, COUNT, FIRST, LAST, RANGE, DIFF, SDIFF, INTEGRAL);
        Collections.addAll(ANALYSES, TREND, OUTLIER, FREQUENCY, FASTDTW, SAX);
        Collections.addAll(TRANSFORMATIONS, VECTOR, DIVIDE, SCALE, BOTTOM, TOP, MOVAVG, DERIVATIVE, NNDERIVATIVE, ADD, SUB, TIMESHIFT, DISTINCT);
        Collections.addAll(STRACE, SPLIT);
        Collections.addAll(LSOF, GROUP);
    }

    /**
     * Checks if the given type is a high level analysis
     *
     * @param type the function type
     * @return true if the analysis type is a high level analysis, otherwise false
     */
    public static boolean isAnalysis(FunctionType type) {
        return ANALYSES.contains(type);
    }

    /**
     * Check if the given type is an aggregation
     *
     * @param type the function type
     * @return true if an aggregation, otherwise false
     */
    public static boolean isAggregation(FunctionType type) {
        return AGGREGATIONS.contains(type);
    }

    /**
     * Checks if the given type is a transformation
     *
     * @param type the function type
     * @return true if the type is a transformation, otherwise false
     */
    public static boolean isTransformation(FunctionType type) {
        return TRANSFORMATIONS.contains(type);
    }

    public static boolean isLsof(FunctionType type) {
        return LSOF.contains(type);
    }

    public static boolean isStrace(FunctionType type) {
        return STRACE.contains(type);
    }
}
