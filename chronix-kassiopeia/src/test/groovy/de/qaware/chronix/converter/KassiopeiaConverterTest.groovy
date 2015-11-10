/*
 *    Copyright (C) 2015 QAware GmbH
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
package de.qaware.chronix.converter

import de.qaware.chronix.dts.Pair
import de.qaware.chronix.timeseries.TimeSeries
import spock.lang.Specification

/**
 * Unit test for the kassiopeia converter
 * @author f.lautenschlager
 */
class KassiopeiaConverterTest extends Specification {

    def "test to and from storage document"() {
        given:
        def converter = new KassiopeiaConverter()
        def points = [Pair.pairOf(1l, 1d),
                      Pair.pairOf(20l, 2d),
                      Pair.pairOf(50l, 3d)]
        def timeSeries = new TimeSeries<Long, Double>(points.iterator())

        timeSeries.addAttribute("host", "NB-Prod-01")
        timeSeries.addAttribute("process", "kassiopeiaConverterTest")
        timeSeries.addAttribute("length", points.size())
        timeSeries.addAttribute("maxValue", 3d)


        when:
        def binaryDocument = converter.to(timeSeries)

        def reconvertedTimeSeries = converter.from(binaryDocument, 1l, 50l);
        then:
        binaryDocument != null
        reconvertedTimeSeries.size() == 4
        reconvertedTimeSeries.get(0).getSecond() == null;
        reconvertedTimeSeries.get(1).getSecond() == 1d
        reconvertedTimeSeries.get(2).getSecond() == 2d
        reconvertedTimeSeries.get(3).getSecond() == 3d

        reconvertedTimeSeries.getAttribute("host") == "NB-Prod-01"
        reconvertedTimeSeries.getAttribute("process") == "kassiopeiaConverterTest"
        reconvertedTimeSeries.getAttribute("length") == 3I
        reconvertedTimeSeries.getAttribute("maxValue") == 3d
    }

    def "test from storage document with range query"() {
        given:
        def converter = new KassiopeiaConverter()
        def points = [Pair.pairOf(1l, 1d),
                      Pair.pairOf(20l, 2d),
                      Pair.pairOf(50l, 3d),
                      Pair.pairOf(80l, 4d),
                      Pair.pairOf(115l, 5d),
                      Pair.pairOf(189l, 6d)]
        def timeSeries = new TimeSeries<Long, Double>(points.iterator())


        when:
        def binaryDocument = converter.to(timeSeries)

        def reconvertedTimeSeries = converter.from(binaryDocument, 50l, 115l);
        then:
        binaryDocument != null
        reconvertedTimeSeries.size() == 4
        reconvertedTimeSeries.get(0).getSecond() == null;
        reconvertedTimeSeries.get(1).getSecond() == 3d
        reconvertedTimeSeries.get(2).getSecond() == 4d
        reconvertedTimeSeries.get(3).getSecond() == 5d
    }

    def "test to with empty time series"() {

        given:
        def converter = new KassiopeiaConverter()
        def points = []

        when:
        def binaryDocument = converter.to(new TimeSeries<Long, Double>(points.iterator()))

        then:
        binaryDocument.data == new byte[0];
    }
}
