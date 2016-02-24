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
package de.qaware.chronix.solr.query.analysis.collectors;

/***
 * A class holding the parsed analysis with its arguments
 *
 * @author f.lautenschlager
 */
public final class ChronixAnalysis {

    private final AnalysisType type;
    private final String[] arguments;
    private final int subqueryIndex;


    /**
     * Constructs a chronix analysis
     *
     * @param type      the analysis type
     * @param arguments the arguments
     */
    public ChronixAnalysis(AnalysisType type, String[] arguments) {
        this.type = type;
        this.arguments = arguments.clone();
        this.subqueryIndex = subqueryIndex(arguments);
    }

    private int subqueryIndex(String[] arguments) {
        if (arguments == null || arguments.length == 0) {
            return -1;
        }

        for (int i = 0; i < arguments.length; i++) {
            String argument = arguments[i];
            if (argument.matches("(.*)")) {
                return i;
            }
        }

        return -1;
    }

    /**
     * @return the analysis type
     */
    public AnalysisType getType() {
        return type;
    }

    /**
     * @return the generic arguments
     */
    public String[] getArguments() {
        return arguments.clone();
    }

    /**
     * @return true if the analysis needs further sub query
     */
    public boolean hasSubquery() {
        return subqueryIndex != -1;
    }

    /**
     * @return the subquery
     */
    public String getSubQuery() {
        return arguments[subqueryIndex];
    }
}
