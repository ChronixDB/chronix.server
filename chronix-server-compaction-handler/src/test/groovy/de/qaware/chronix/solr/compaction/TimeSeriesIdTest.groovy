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
package de.qaware.chronix.solr.compaction

import spock.lang.Specification

/**
 * Test case for {@link TimeSeriesId}.
 *
 * @author alex.christ
 */
class TimeSeriesIdTest extends Specification {

    def "test toQuery and toString"() {
        given:
        def id = new TimeSeriesId([metric: 'cpu_load', host: 'machine1', process: 'java'])

        expect:
        id.toString() == '[host:machine1,metric:cpu_load,process:java]'
        id.toQuery() == 'metric:"cpu_load" AND host:"machine1" AND process:"java"'
    }
}