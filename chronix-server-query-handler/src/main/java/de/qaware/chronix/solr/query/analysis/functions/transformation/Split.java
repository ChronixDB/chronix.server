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
package de.qaware.chronix.solr.query.analysis.functions.transformation;

import de.qaware.chronix.solr.query.analysis.FunctionValueMap;
import de.qaware.chronix.solr.query.analysis.functions.ChronixListTransformation;
import de.qaware.chronix.solr.query.analysis.functions.FunctionType;
import de.qaware.chronix.timeseries.StraceTimeSeries;

/**
 * Created by f.lautenschlager on 25.09.2016.
 */
public class Split implements ChronixListTransformation<StraceTimeSeries> {

    /*@Override
    public List<StraceTimeSeries> exeute(StraceTimeSeries timeSeries, FunctionValueMap analysisAndValues) {
        return new ArrayList<>(timeSeries.split());
    }
    */

    @Override
    public FunctionType getType() {
        return FunctionType.SPLIT;
    }

    @Override
    public void execute(StraceTimeSeries timeSeries, FunctionValueMap analysisAndValues) {
        //TODO: How to store this?
    }

    @Override
    public String[] getArguments() {
        return new String[0];
    }

}
