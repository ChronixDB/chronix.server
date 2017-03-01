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
package de.qaware.chronix.solr.type.metric;

/**
 * The Chronix Query parameter constants
 *
 * @author f.lautenschlager
 */
public final class ChronixQueryParams {

    public static final String TYPE_NAME = "metric";

    /**
     * The solr version field. We remove that field in the function result
     */
    public static final String SOLR_VERSION_FIELD = "_version_";
    public static final String DATA_AS_JSON = "dataAsJson";


    private ChronixQueryParams() {
        //avoid instances
    }

}