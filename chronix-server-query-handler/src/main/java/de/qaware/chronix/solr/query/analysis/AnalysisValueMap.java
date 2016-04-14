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

import de.qaware.chronix.solr.query.analysis.functions.ChronixAnalysis;

/**
 * Simple fixed size map of chronix analysis and value
 *
 * @author f.lautenschlager
 */
public class AnalysisValueMap {

    private final ChronixAnalysis[] analyses;
    private final double[] values;
    private final String[] identifiers;
    private int size = 0;

    public AnalysisValueMap(int size) {
        this.analyses = new ChronixAnalysis[size];
        this.values = new double[size];
        this.identifiers = new String[size];
    }

    /**
     * Appends the
     *
     * @param analysis the chronix analysis
     * @param value    the value for the analysis
     */
    public void add(ChronixAnalysis analysis, double value, String identifier) {
        if (size < analyses.length) {
            analyses[size] = analysis;
            values[size] = value;
            identifiers[size] = identifier;
            size++;
        } else {
            throw new IndexOutOfBoundsException("Try to put analysis to map with max size " + analyses.length + " but index " + size + " is out of range.");
        }
    }

    /**
     * @param i the index
     * @return the analysis at index i
     */
    public ChronixAnalysis getAnalysis(int i) {
        return analyses[i];
    }

    /**
     * @param i the index
     * @return the value at index i
     */
    public double getValue(int i) {
        return values[i];
    }

    /**
     * @param i the index
     * @return the additional identifier
     */
    public String getIdentifier(int i) {
        return identifiers[i];
    }

    /**
     * @return the size of the map
     */
    public int size() {
        return size;
    }


}
