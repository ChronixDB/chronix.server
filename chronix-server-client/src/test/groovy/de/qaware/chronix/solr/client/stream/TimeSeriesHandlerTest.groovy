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
package de.qaware.chronix.solr.client.stream

import de.qaware.chronix.solr.test.extensions.ReflectionHelper
import org.slf4j.Logger
import spock.lang.Specification

import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

/**
 * Unit test for the time series handler
 * @author f.lautenschlager
 */
class TimeSeriesHandlerTest extends Specification {
    def "test onSuccess and take"() {
        given:
        def timeSeriesHandler = new TimeSeriesHandler(1)

        when:
        timeSeriesHandler.onSuccess("something")

        then:
        timeSeriesHandler.take() == "something"
    }

    def "test onFailure"() {
        given:
        def timeSeriesHandler = new TimeSeriesHandler(1)
        def logger = Mock(Logger.class)

        when:
        ReflectionHelper.setValueToFieldOfObject(logger, "LOGGER", timeSeriesHandler)
        timeSeriesHandler.onFailure(new Exception("Test-Exception"))

        then:
        1 * logger.warn(_ as String, _ as Throwable)
    }

    def "test take with interrupted exception"() {
        given:
        def timeSeriesHandler = new TimeSeriesHandler(1)
        def queue = Mock(BlockingQueue.class)
        def logger = Mock(Logger.class)

        when:
        queue.poll(1l, TimeUnit.MINUTES) >> { throw new InterruptedException("Test-Exception") }
        ReflectionHelper.setValueToFieldOfObject(queue, "queue", timeSeriesHandler)
        ReflectionHelper.setValueToFieldOfObject(logger, "LOGGER", timeSeriesHandler)

        def result = timeSeriesHandler.take()

        then:
        0 * logger.debug(_ as String)
        1 * logger.warn(_ as String, _ as Throwable)
        result == null
        thrown IllegalStateException

    }

    def "test onSuccess with interrupted exception"() {
        given:
        def timeSeriesHandler = new TimeSeriesHandler(1)
        def queue = Mock(BlockingQueue.class)
        def logger = Mock(Logger.class)

        when:
        queue.put(_ as Object) >> { throw new InterruptedException("Test-Exception") }
        ReflectionHelper.setValueToFieldOfObject(queue, "queue", timeSeriesHandler)
        ReflectionHelper.setValueToFieldOfObject(logger, "LOGGER", timeSeriesHandler)

        def result = timeSeriesHandler.onSuccess("Something")

        then:
        1 * logger.warn(_ as String, _ as Throwable)
        result == null
    }


}
