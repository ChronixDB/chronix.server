/*
 * Copyright (C) 2016 QAware GmbH
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package de.qaware.chronix.solr.query.analysis;

import de.qaware.chronix.solr.query.analysis.functions.ChronixAggregation;
import de.qaware.chronix.solr.query.analysis.functions.ChronixAnalysis;
import de.qaware.chronix.solr.query.analysis.functions.ChronixTransformation;

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
 * @param <T> the type to apply the function to
 * @author f.lautenschlager
 */
class QueryFunctions<T> {

    private Set<ChronixAnalysis<T>> analyses;
    private Set<ChronixAggregation<T>> aggregations;
    private List<ChronixTransformation<T>> transformations;

    QueryFunctions() {
        analyses = new HashSet<>();
        aggregations = new HashSet<>();
        transformations = new ArrayList<>();
    }

    /**
     * @return the analyses in the query
     */
    public Set<ChronixAnalysis<T>> getAnalyses() {
        return analyses;
    }

    /**
     * @return the aggregations in the query
     */
    public Set<ChronixAggregation<T>> getAggregations() {
        return aggregations;
    }

    /**
     * @return the transformations in the query
     */
    public List<ChronixTransformation<T>> getTransformations() {
        return transformations;
    }

    /**
     * Add the given analysis to the query functions
     *
     * @param analysis the analysis
     */
    public void addAnalysis(ChronixAnalysis<T> analysis) {
        this.analyses.add(analysis);
    }

    /**
     * Add the given aggregation to the query functions
     *
     * @param aggregation the aggregation
     */
    public void addAggregation(ChronixAggregation<T> aggregation) {
        this.aggregations.add(aggregation);
    }

    /**
     * Add the given transformation to the query functions
     *
     * @param transformation the transformation
     */
    public void addTransformation(ChronixTransformation<T> transformation) {
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
