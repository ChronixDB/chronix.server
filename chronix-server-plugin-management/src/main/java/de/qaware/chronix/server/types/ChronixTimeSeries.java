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
package de.qaware.chronix.server.types;

import de.qaware.chronix.server.functions.ChronixAggregation;
import de.qaware.chronix.server.functions.ChronixFunction;
import de.qaware.chronix.server.functions.FunctionValueMap;
import org.apache.solr.common.SolrDocument;

import java.util.Map;

/**
 * The chronix time series interface
 *
 * @author f.lautenschlager
 */
public interface ChronixTimeSeries {


    /**
     * Applies the given transformation on the time series
     *
     * @param transformation the actual transformation
     * @param functionValues the functionValues to add the result
     */
    void applyTransformation(ChronixFunction transformation, FunctionValueMap functionValues);

    /**
     * Applies the given aggregation on the time series
     *
     * @param aggregation    the actual aggregation
     * @param functionValues the functionValues to add the result
     */
    void applyAggregation(ChronixAggregation aggregation, FunctionValueMap functionValues);

    /**
     * Applies the actual analysis
     *
     * @param analysis
     * @param functionValueMap
     */
    void applyAnalysis(ChronixFunction analysis, FunctionValueMap functionValueMap);

    /**
     * @param analysis
     * @param subQueryTimeSeries
     * @param functionValues
     */
    void applyPairAnalysis(ChronixFunction analysis, ChronixTimeSeries subQueryTimeSeries, FunctionValueMap functionValues);

    /**
     * @param key
     * @param dataShouldReturned
     * @param dataAsJson
     * @return a solr document
     */
    SolrDocument convert(String key, boolean dataShouldReturned, boolean dataAsJson);

    /**
     * @return the type of the chronix time series
     */
    String getType();

    /**
     * @return the name of the time series
     */
    String getName();

    /**
     * @return the start of the time series
     */
    long getStart();

    /**
     * @return the end of the time series
     */
    long getEnd();

    /**
     * @return the attributes
     */
    Map<String, Object> attributes();

    /**
     * Sorts the time series by time acceding
     */
    void sort();

    /**
     * @return the data as json
     */
    String dataAsJson();

    /**
     * @return as binary large object
     */
    byte[] dataAsBlob();

}
