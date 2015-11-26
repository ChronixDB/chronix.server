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
package de.qaware.chronix.solr.retention;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * The quartz job to delete old time series document from solr.
 *
 * @author f.lautenschlager
 */
public class RetentionJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetentionJob.class);

    private final CloseableHttpClient httpClient;

    /**
     * Constructs a retention job
     */
    public RetentionJob() {
        httpClient = HttpClients.createDefault();
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("Starting retention job");
        JobDataMap data = context.getMergedJobDataMap();

        String url = data.getString(RetentionConstants.RETENTION_URL);
        HttpGet httpget = new HttpGet(url);

        try (CloseableHttpResponse response = httpClient.execute(httpget)) {
            LOGGER.info("Response was {}", response);
        } catch (IOException e) {
            throw new JobExecutionException("Could not execute http get request " + httpget, e);
        }

    }
}
