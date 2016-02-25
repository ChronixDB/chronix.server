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
    Object[] getArguments();

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
