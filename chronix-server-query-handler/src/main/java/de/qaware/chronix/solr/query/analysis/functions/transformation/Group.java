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
import de.qaware.chronix.solr.query.analysis.functions.ChronixTransformation;
import de.qaware.chronix.solr.query.analysis.functions.FunctionType;
import de.qaware.chronix.timeseries.Lsof;
import de.qaware.chronix.timeseries.LsofTimeSeries;

import java.util.*;

/**
 * Created by f.lautenschlager on 25.09.2016.
 */
public class Group implements ChronixTransformation<LsofTimeSeries> {


    private final Set<String> filterValues;
    private final String field;

    public Group(String field, String[] values) {
        this.field = field;
        this.filterValues = new HashSet<>(Arrays.asList(values));
    }

    @Override
    public void execute(LsofTimeSeries timeSeries, FunctionValueMap analysisAndValues) {

        Map<String, List<Lsof>> values = timeSeries.groupBy(field);

        for (Map.Entry<String, List<Lsof>> entry : values.entrySet()) {

            //Wildcard
            if (filterValues.contains("*")) {
                timeSeries.getAttributesReference().put(entry.getKey(), entry.getValue().size());
            } else if (filterValues.contains(entry.getKey())) {
                timeSeries.getAttributesReference().put(entry.getKey(), entry.getValue().size());
            }
        }
        analysisAndValues.add(this);

    }

    @Override
    public FunctionType getType() {
        return FunctionType.GROUP;
    }

    @Override
    public String[] getArguments() {
        return new String[]{"field=" + field, "filters=" + filterValues};
    }
}
