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
package de.qaware.chronix.solr.test.converter;

import de.qaware.chronix.converter.BinaryTimeSeries;
import de.qaware.chronix.converter.TimeSeriesConverter;

/**
 * The default time series converter, that does nothing.
 * Only for test purposes.
 *
 * @author f.lautenschlager
 */
public class DefaultTimeSeriesConverter implements TimeSeriesConverter<BinaryTimeSeries> {


    @Override
    public BinaryTimeSeries from(BinaryTimeSeries binaryTimeSeries, long queryStart, long queryEnd) {
        //Just return the binary time series
        return binaryTimeSeries;
    }

    @Override
    public BinaryTimeSeries to(BinaryTimeSeries timeSeries) {
        //Just return the binary time series
        return timeSeries;
    }
}

