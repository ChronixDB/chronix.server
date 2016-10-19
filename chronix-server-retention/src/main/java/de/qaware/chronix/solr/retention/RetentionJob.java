/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
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

    /**
     * Executes the job that calls the retention plugin.
     *
     * @param context the current job context
     * @throws JobExecutionException if the solr server could not be reached.
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("Starting retention job");
        JobDataMap data = context.getMergedJobDataMap();

        String url = data.getString(RetentionConstants.RETENTION_URL);
        HttpGet httpget = new HttpGet(url);

        try {
            CloseableHttpResponse response = httpClient.execute(httpget);
            LOGGER.info("Response was {}", response);
        } catch (IOException e) {
            throw new JobExecutionException("Could not execute http get request " + httpget, e);
        }

    }
}
