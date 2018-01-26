/*
 * Copyright (C) 2018 QAware GmbH
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
package de.qaware.chronix.server.functions;

public class FunctionCtxEntry {

    private final ChronixTransformation[] transformations;
    private final ChronixAggregation[] aggregations;
    private final ChronixAnalysis[] analyses;
    private final ChronixFilter[] filters;
    private final double[] aggregationValues;
    private final boolean[] analysisValues;
    private int transformationSize;
    private int aggregationSize;
    private int analysisSize;
    private int filterSize;


    FunctionCtxEntry(int maxAmountOfTransformations, int maxAmountOfAggregations, int maxAmountOfAnalyes, int maxAmaountOfFilters) {
        this.transformations = new ChronixTransformation[maxAmountOfTransformations];
        this.aggregations = new ChronixAggregation[maxAmountOfAggregations];
        this.analyses = new ChronixAnalysis[maxAmountOfAnalyes];
        this.filters = new ChronixFilter[maxAmaountOfFilters];

        this.analysisValues = new boolean[maxAmountOfAnalyes];
        this.aggregationValues = new double[maxAmountOfAggregations];


    }

    public void add(ChronixTransformation transformation) {
        if (transformationSize < transformations.length) {
            transformations[transformationSize] = transformation;
            transformationSize++;
        } else {
            throw new IndexOutOfBoundsException("Try to put transformation to map with max size " + transformations.length + " but index " + transformationSize + " is out of range.");
        }
    }

    /**
     * Gets the transformation
     *
     * @param i the index
     * @return the transformation at index i
     */
    public ChronixTransformation getTransformation(int i) {
        return transformations[i];
    }


    /**
     * @return the size of the transformations
     */
    public int sizeOfTransformations() {
        return transformationSize;
    }

    /**
     * @return the total amount of functions
     */
    public int size() {
        return analysisSize + transformationSize + aggregationSize + filterSize;
    }

    public void add(ChronixAggregation aggregation, double value) {

        if (aggregationSize < aggregations.length) {
            aggregations[aggregationSize] = aggregation;
            aggregationValues[aggregationSize] = value;
            aggregationSize++;
        } else {
            throw new IndexOutOfBoundsException("Try to put aggregation to map with max size " + aggregations.length + " but index " + aggregationSize + " is out of range.");
        }
    }

    /**
     * @return the size of the aggregations
     */
    public int sizeOfAggregations() {
        return aggregationSize;
    }

    public void add(ChronixAnalysis analysis, boolean value) {
        if (analysisSize < analyses.length) {
            analyses[analysisSize] = analysis;
            analysisValues[analysisSize] = value;
            analysisSize++;
        } else {
            throw new IndexOutOfBoundsException("Try to put analysis to map with max size " + analyses.length + " but index " + analysisSize + " is out of range.");
        }
    }

    public void add(ChronixFilter filter) {
        if(filterSize < filters.length) {
            filters[filterSize] = filter;
            filterSize++;
        } else {
            throw new IndexOutOfBoundsException("Try to put filter to map with max size " + filters.length + " but index " + filterSize + " is out of range.");
        }

    }

    /**
     * @return the size of the filters
     */
    public int sizeOfFilters() {
        return filterSize;
    }

    /**
     * Gets the filter
     *
     * @param i the index
     * @return the filter at index i
     */
    public ChronixFilter getFilter(int i)
    {
        return filters[i];
    }

    /**
     * Gets the analysis at the index position
     *
     * @param i the index
     * @return the analysis at index i
     */
    public ChronixAnalysis getAnalysis(int i) {
        return analyses[i];
    }


    /**
     * Gets the analysis value at the index position
     *
     * @param i the index
     * @return the value at index i
     */
    public boolean getAnalysisValue(int i) {
        return analysisValues[i];
    }

    /**
     * Gets the aggregation at the index position
     *
     * @param i the index
     * @return the aggregation at index i
     */
    public ChronixAggregation getAggregation(int i) {
        return aggregations[i];
    }

    /**
     * Gets the aggregation value at the index position
     *
     * @param i the index
     * @return the value at index i
     */
    public double getAggregationValue(int i) {
        return aggregationValues[i];
    }


    /**
     * @return the size of the analyses
     */
    public int sizeOfAnalyses() {
        return analysisSize;
    }
}
