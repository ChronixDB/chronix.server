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
package de.qaware.chronix.solr.retention;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.solr.common.params.UpdateParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.update.CommitUpdateCommand;
import org.apache.solr.update.DeleteUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorChain;
import org.apache.solr.util.DateMathParser;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import static org.quartz.CronScheduleBuilder.dailyAtHourAndMinute;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * This update handler removes documents that reaches a maximum age defined in the solrconfig.xml
 * <p>
 * A example configuration could be:
 * <code>
 * <requestHandler name="/retention"
 * class="de.qaware.chronix.solr.retention.ChronixRetentionHandler">
 * <lst name="defaults">
 * <str name="echoParams">explicit</str>
 * <str name="fl">*,score</str>
 * <str name="wt">xml</str>
 * </lst>
 * <lst name="invariants">
 * <str name="queryField">end</str>
 * <str name="timeSeriesAge">40DAY</str>
 * <str name="removeDailyAt">12</str>
 * <str name="retentionUrl">http://localhost:8983/solr/chronix/retention</str>
 * <str name="softCommit">false</str>
 * <str name="optimizeAfterDeletion">false</str>
 * </lst>
 * </requestHandler>
 * </code>
 *
 * @author f.lautenschlager
 */
public class ChronixRetentionHandler extends RequestHandlerBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChronixRetentionHandler.class);

    /**
     * Fields to create the deletion query an manage the process
     */
    private String queryField;
    private String timeSeriesAge;
    private boolean optimizeAfterDeletion = false;
    private Boolean softCommit;
    private Scheduler scheduler;
    private int removeDailyAt;
    private String retentionURL;


    @Override
    public void init(NamedList args) {
        super.init(args);
        this.queryField = invariants.get(RetentionConstants.QUERY_FIELD);
        this.timeSeriesAge = invariants.get(RetentionConstants.REMOVE_TIME_SERIES_OLDER);
        this.optimizeAfterDeletion = Boolean.valueOf(invariants.get(RetentionConstants.OPTIMIZE_AFTER_DELETION));
        this.softCommit = Boolean.valueOf(invariants.get(RetentionConstants.SOFT_COMMIT));
        this.removeDailyAt = Integer.valueOf(invariants.get(RetentionConstants.REMOVE_DAILY_AT));
        this.retentionURL = String.valueOf(invariants.get(RetentionConstants.RETENTION_URL));

        scheduledDeletion();
        addShutdownHook();
    }

    /**
     * This method sets up a scheduled deletion job.
     */
    private void scheduledDeletion() {
        System.setProperty("org.quartz.threadPool.threadCount", "3");
        SchedulerFactory sf = new StdSchedulerFactory();
        try {
            scheduler = sf.getScheduler();

            Trigger trigger = newTrigger()
                    .withIdentity("Data_Retention_Trigger")
                    .startNow()
                    .withSchedule(dailyAtHourAndMinute(removeDailyAt, 0))
                    .build();

            JobDetail deletionJob = newJob(RetentionJob.class)
                    .withIdentity("Data_Retention_Job")
                    .usingJobData(RetentionConstants.RETENTION_URL, retentionURL)
                    .build();

            scheduler.scheduleJob(deletionJob, trigger);

            scheduler.startDelayed(180);
        } catch (SchedulerException e) {
            LOGGER.warn("Got an scheduler exception.", e);
        }
    }

    /**
     * Adds an shutdown hook to stop the deletion job
     */
    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                scheduler.shutdown(false);
            } catch (SchedulerException e) {
                LOGGER.warn("Could not add shutdown hook data retention thread.", e);
            }
        }));
    }

    /**
     * Processes the request for the round robin update update handler.
     *
     * @param req - the solr query request information
     * @param rsp - the solr query response information
     * @throws ParseException,IOException,SyntaxError - if bad things happen
     */
    @Override
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws ParseException, IOException, SyntaxError {
        String deletionQuery = getDeletionQuery();
        LOGGER.info("Handle deletion request for query {}", deletionQuery);

        if (olderDocumentsExists(deletionQuery, req, rsp)) {
            UpdateRequestProcessor processor = getProcessor(req, rsp);
            deleteOldDocuments(deletionQuery, processor, req);
            commitDeletions(processor, req);
        }
    }

    /**
     * Searches the index, if older documents exists. Updates the solr query response.
     *
     * @param req - the solr query request information
     * @param rsp - the solr query response information
     * @return true if the hit count is greater zero, otherwise false
     * @throws SyntaxError, IOException if bad things happen
     */
    private boolean olderDocumentsExists(String queryString, SolrQueryRequest req, SolrQueryResponse rsp) throws SyntaxError, IOException {
        String defType = req.getParams().get(QueryParsing.DEFTYPE, QParserPlugin.DEFAULT_QTYPE);

        QParser queryParser = QParser.getParser(queryString, defType, req);
        Query query = queryParser.getQuery();

        TotalHitCountCollector totalHitCountCollector = new TotalHitCountCollector();
        req.getSearcher().search(query, totalHitCountCollector);

        rsp.add("query", String.format("%s:[* TO NOW-%s]", queryField, timeSeriesAge));
        rsp.add("queryTechnical", queryString);
        rsp.add("removedDocuments", totalHitCountCollector.getTotalHits());

        return totalHitCountCollector.getTotalHits() != 0;
    }

    /**
     * Gets the processor build from the processor update chain(UpdateParams.UPDATE_CHAIN)
     *
     * @param req - the solr query request information
     * @param rsp - the solr query response information
     * @return the update request processor
     */
    private UpdateRequestProcessor getProcessor(SolrQueryRequest req, SolrQueryResponse rsp) {
        UpdateRequestProcessorChain processorChain =
                req.getCore().getUpdateProcessingChain(req.getParams().get(UpdateParams.UPDATE_CHAIN));
        return processorChain.createProcessor(req, rsp);
    }

    /**
     * Triggers the deletion
     *
     * @param processor the update processor do process deletions
     * @param req       the solr query request information
     * @throws IOException if bad things happen
     */
    private void deleteOldDocuments(String deletionQuery, UpdateRequestProcessor processor, SolrQueryRequest req) throws IOException {
        DeleteUpdateCommand delete = new DeleteUpdateCommand(req);
        delete.setQuery(deletionQuery);
        processor.processDelete(delete);
    }

    /**
     * Triggers a commit to make the deletion visible on the index
     *
     * @param processor the update processor do process deletions
     * @param req       the solr query request information
     * @throws IOException if bad things happen
     */
    private void commitDeletions(UpdateRequestProcessor processor, SolrQueryRequest req) throws IOException {
        CommitUpdateCommand commit = new CommitUpdateCommand(req, optimizeAfterDeletion);
        commit.softCommit = softCommit;
        processor.processCommit(commit);
    }

    /**
     * Builds the deletion query for documents that are older than timeSeriesAge
     *
     * @return the deletion query
     * @throws ParseException if the term is not a solr date maths expression
     */
    private String getDeletionQuery() throws ParseException {
        return String.format("%s:[* TO %d]", queryField, parseEndDate().getTime());
    }

    /**
     * Parses the timeSeriesAge term with solr date math
     *
     * @return the end date for the range query
     * @throws ParseException if the term is not a solr date math expression
     */
    private Date parseEndDate() throws ParseException {
        return new DateMathParser().parseMath(String.format("+0MILLISECOND-%s", timeSeriesAge));
    }

    @Override
    public String getDescription() {
        return "The Chronix retention plugin.";
    }

    @Override
    public String getSource() {
        return "www.chronix.io";
    }
}
