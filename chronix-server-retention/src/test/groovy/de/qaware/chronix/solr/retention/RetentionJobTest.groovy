/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
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
