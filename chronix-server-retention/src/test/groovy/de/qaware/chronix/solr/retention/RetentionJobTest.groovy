/*
 * Copyright (C) 2015 QAware GmbH
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
package de.qaware.chronix.solr.retention

import de.qaware.chronix.solr.test.extensions.ReflectionHelper
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.quartz.JobDataMap
import org.quartz.JobExecutionException
import org.quartz.impl.JobExecutionContextImpl
import spock.lang.Specification

/**
 * Unit test fot the retention job
 * @author f.lautenschlager
 */
class RetentionJobTest extends Specification {

    def "test execute"() {
        given:
        def retentionJob = new RetentionJob()
        def jobContext = Mock(JobExecutionContextImpl)
        def httpClient = Mock(CloseableHttpClient)

        def jobDataMap = new JobDataMap()
        jobDataMap.put(RetentionConstants.RETENTION_URL, "http://www.google.de")
        jobContext.getMergedJobDataMap() >> jobDataMap

        ReflectionHelper.setValueToFieldOfObject(httpClient, "httpClient", retentionJob)

        when:
        2.times {
            retentionJob.execute(jobContext)
        }

        then:
        2 * httpClient.execute(_ as HttpGet)
    }

    def "test with response exception"() {
        given:
        def retentionJob = new RetentionJob()
        def jobContext = Mock(JobExecutionContextImpl)
        def httpClient = Mock(CloseableHttpClient)

        def jobDataMap = new JobDataMap()
        jobDataMap.put(RetentionConstants.RETENTION_URL, "http://www.google.de")
        jobContext.getMergedJobDataMap() >> jobDataMap

        ReflectionHelper.setValueToFieldOfObject(httpClient, "httpClient", retentionJob)

        httpClient.execute(_ as HttpGet) >> { throw new IOException("Test Exception") }
        when:
        retentionJob.execute(jobContext)

        then:
        thrown JobExecutionException
    }

    def "test with real integration"() {
        given:
        def retentionJob = new RetentionJob()

        when:
        def exceptionThrown = false
        try {
            retentionJob.execute(jobContext)
        } catch (JobExecutionException e) {
            exceptionThrown = true
        }

        then:
        exceptionThrown == result

        where:
        jobContext << ["create valid job context"(), "create invalid job context"(), "create valid job context"()]
        result << [false, true, false]
    }

    def "create valid job context"() {
        def jobDataMap = new JobDataMap()
        jobDataMap.put(RetentionConstants.RETENTION_URL, "http://www.google.de")
        def jobContext = Mock(JobExecutionContextImpl)
        jobContext.getMergedJobDataMap() >> jobDataMap

        jobContext
    }

    def "create invalid job context"() {
        def jobDataMap = new JobDataMap()
        jobDataMap.put(RetentionConstants.RETENTION_URL, "http://www.invalid.iknow")
        def jobContext = Mock(JobExecutionContextImpl)
        jobContext.getMergedJobDataMap() >> jobDataMap

        jobContext
    }

}
