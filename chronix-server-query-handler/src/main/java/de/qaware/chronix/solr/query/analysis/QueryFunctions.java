/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis;

import de.qaware.chronix.solr.query.analysis.functions.ChronixAnalysis;
import de.qaware.chronix.solr.query.analysis.functions.ChronixFunction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class that holds the query functions
 * - aggregations
 * - analyses
 * - transformations
 *
 * @author f.lautenschlager
 */
class QueryFunctions {

    private Set<ChronixAnalysis> analyses;
    private Set<ChronixFunction> aggregations;
    private List<ChronixFunction> transformations;

    QueryFunctions() {
        analyses = new HashSet<>();
        aggregations = new HashSet<>();
        transformations = new ArrayList<>();
    }

    /**
     * @return the analyses in the query
     */
    public Set<ChronixAnalysis> getAnalyses() {
        return analyses;
    }

    /**
     * @return the aggregations in the query
     */
    public Set<ChronixFunction> getAggregations() {
        return aggregations;
    }

    /**
     * @return the transformations in the query
     */
    public List<ChronixFunction> getTransformations() {
        return transformations;
    }

    /**
     * Add the given analysis to the query functions
     *
     * @param analysis the analysis
     */
    public void addAnalysis(ChronixAnalysis analysis) {
        this.analyses.add(analysis);
    }

    /**
     * Add the given aggregation to the query functions
     *
     * @param aggregation the aggregation
     */
    public void addAggregation(ChronixFunction aggregation) {
        this.aggregations.add(aggregation);
    }

    /**
     * Add the given transformation to the query functions
     *
     * @param transformation the transformation
     */
    public void addTransformation(ChronixFunction transformation) {
        this.transformations.add(transformation);
    }

    /**
     * @return true if all (aggregations, analyses, transformations) are emtpy, otherwise false
     */
    public boolean isEmpty() {
        return transformations.isEmpty() && aggregations.isEmpty() && analyses.isEmpty();
    }

    /**
     * @return true if the functions contains transformations
     */
    public boolean containsTransformations() {
        return !transformations.isEmpty();
    }

    /**
     * @return true if the functions contains aggregations
     */
    public boolean containsAggregations() {
        return !aggregations.isEmpty();
    }

    /**
     * @return true if the functions contains analyses
     */
    public boolean containsAnalyses() {
        return !analyses.isEmpty();
    }

    /**
     * @return the size of all (aggregations, analyses, transformations)
     */
    public int size() {
        return sizeOfAggregations() + sizeOfAnalyses() + sizeOfTransformations();
    }

    /**
     * @return the size of the transformations
     */
    public int sizeOfTransformations() {
        return transformations.size();
    }

    /**
     * @return the amount of aggregations
     */
    public int sizeOfAggregations() {
        return aggregations.size();
    }

    /**
     * @return the size of analyses
     */
    public int sizeOfAnalyses() {
        return analyses.size();
    }
}
