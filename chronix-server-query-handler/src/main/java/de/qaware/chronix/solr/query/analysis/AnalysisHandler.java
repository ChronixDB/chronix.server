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
package de.qaware.chronix.solr.query.analysis;

import de.qaware.chronix.Schema;
import de.qaware.chronix.solr.query.ChronixQueryParams;
import de.qaware.chronix.solr.query.analysis.functions.ChronixAnalysis;
import de.qaware.chronix.solr.query.analysis.functions.ChronixTransformation;
import de.qaware.chronix.solr.query.date.DateQueryParser;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.handler.component.SearchHandler;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.DocList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;

/**
 * Analysis search handler
 *
 * @author f.lautenschlager
 */
public class AnalysisHandler extends SearchHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisHandler.class);

    private final DocListProvider docListProvider;

    private final DateQueryParser subQueryDateRangeParser = new DateQueryParser(new String[]{ChronixQueryParams.DATE_START_FIELD, ChronixQueryParams.DATE_END_FIELD});

    private static final String DATA_WITH_LEADING_AND_TRAILING_COMMA = "," + Schema.DATA + ",";

    /**
     * Constructs an isAggregation handler
     *
     * @param docListProvider - the search provider for the DocList Result
     */
    public AnalysisHandler(DocListProvider docListProvider) {
        this.docListProvider = docListProvider;
    }

    /**
     * Executes the user search request.
     *
     * @param req the solr query request
     * @param rsp the solr query response holding the result
     * @throws Exception if bad things happen
     */
    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        LOGGER.debug("Handling analysis request {}", req);
        //First check if the request should return documents => rows > 0
        String rowsParam = req.getParams().get(CommonParams.ROWS, null);
        int rows = -1;
        if (rowsParam != null) {
            rows = Integer.parseInt(rowsParam);
        }

        SolrDocumentList results = new SolrDocumentList();
        String[] filterQueries = req.getParams().getParams(CommonParams.FQ);

        //Check if the data field should be returned - default is true
        String fields = req.getParams().get(CommonParams.FL, Schema.DATA);
        boolean dataShouldReturned = fields.contains(DATA_WITH_LEADING_AND_TRAILING_COMMA);

        //Do a query and collect them on the join function
        Function<SolrDocument, String> key = JoinFunctionEvaluator.joinFunction(filterQueries);
        Map<String, List<SolrDocument>> collectedDocs = collectDocuments(req, key);

        //If no rows should returned, we only return the num found
        if (rows == 0) {
            results.setNumFound(collectedDocs.keySet().size());
        } else {
            //Otherwise return the analyzed time series
            final QueryFunctions<MetricTimeSeries> queryFunctions = QueryEvaluator.extractFunctions(filterQueries);
            final List<SolrDocument> resultDocuments = analyze(req, queryFunctions, key, collectedDocs, dataShouldReturned);
            results.addAll(resultDocuments);
            //As we have to analyze all docs in the query at once,
            // the number of documents is also the number of documents found
            results.setNumFound(resultDocuments.size());
        }
        //Add the results to the response
        rsp.add("response", results);
    }

    /**
     * Analyzes the given request using the chronix functions.
     *
     * @param req                the solr request with all information
     * @param functions          the chronix analysis that is applied
     * @param key                the key for joining documents
     * @param collectedDocs      the prior collected documents of the query
     * @param dataShouldReturned flag to indicate if the data should be returned
     * @return a list containing the analyzed time series as solr documents
     * @throws IOException              if bad things happen in querying the documents
     * @throws IllegalArgumentException if the given analysis is not defined
     * @throws ParseException           when the start / end within the sub query could not be parsed
     */
    private List<SolrDocument> analyze(SolrQueryRequest req, QueryFunctions<MetricTimeSeries> functions, Function<SolrDocument, String> key, Map<String, List<SolrDocument>> collectedDocs, boolean dataShouldReturned) throws IOException, IllegalStateException, ParseException {

        if (functions.isEmpty()) {
            LOGGER.info("Functions are empty. Returning empty result");
            return new ArrayList<>();
        }

        final SolrParams params = req.getParams();
        final long queryStart = Long.parseLong(params.get(ChronixQueryParams.QUERY_START_LONG));
        final long queryEnd = Long.parseLong(params.get(ChronixQueryParams.QUERY_END_LONG));
        final List<SolrDocument> resultDocuments = Collections.synchronizedList(new ArrayList<>(collectedDocs.size()));

        collectedDocs.entrySet().parallelStream().forEach(docs -> {
            try {
                MetricTimeSeries timeSeries = AnalysisDocumentBuilder.collectDocumentToTimeSeries(queryStart, queryEnd, docs.getValue());
                AnalysisValueMap analysisAndValues = new AnalysisValueMap(functions.sizeOfAggregations() + functions.sizeOfAnalyses());

                //first we do the transformations
                if (functions.containsTransformations()) {
                    for (ChronixTransformation<MetricTimeSeries> transformation : functions.getTransformations()) {
                        //transform the time series
                        timeSeries = transformation.transform(timeSeries);
                    }
                }

                if (functions.containsAggregations()) {
                    //run over the aggregations
                    for (ChronixAnalysis<MetricTimeSeries> aggregation : functions.getAggregations()) {
                        //Execute analysis and store the result
                        double value = aggregation.execute(timeSeries);
                        analysisAndValues.add(aggregation, value, null);
                    }
                }

                //then we apply aggregations and analyses
                if (functions.containsAnalyses()) {
                    //That is the time series we operate on
                    final MetricTimeSeries transformedTimeSeries = timeSeries;

                    //run over the analyses
                    for (ChronixAnalysis<MetricTimeSeries> analysis : functions.getAnalyses()) {

                        if (analysis.needSubquery()) {
                            //lets parse the sub-query for start and and end terms
                            String modifiedSubQuery = subQueryDateRangeParser.replaceRangeQueryTerms(analysis.getSubquery());
                            Map<String, List<SolrDocument>> subQueryDocuments = collectDocuments(modifiedSubQuery, req, key);

                            //execute the analysis with all sub documents
                            subQueryDocuments.entrySet().parallelStream().forEach(subDocs -> {
                                //Only if have a different time series
                                if (!docs.getKey().equals(subDocs.getKey())) {
                                    MetricTimeSeries subTimeSeries = AnalysisDocumentBuilder.collectDocumentToTimeSeries(queryStart, queryEnd, subDocs.getValue());

                                    double value = analysis.execute(transformedTimeSeries, subTimeSeries);
                                    analysisAndValues.add(analysis, value, subTimeSeries.getMetric());
                                }

                            });

                        } else {
                            //Execute analysis and store the result
                            double value = analysis.execute(timeSeries);
                            analysisAndValues.add(analysis, value, null);
                        }
                    }
                }

                //Here we have to build the document with the results of the analyses
                SolrDocument doc = AnalysisDocumentBuilder.buildDocument(timeSeries, analysisAndValues, docs.getKey(), dataShouldReturned);

                if (doc != null) {
                    resultDocuments.add(doc);
                }

            } catch (ParseException | IOException e) {
                LOGGER.info("Could not parse query due to an exception", e);
            }
        });
        return resultDocuments;
    }

    /**
     * Collects the document matching the given solr query request by using the given collection key function.
     *
     * @param req           the solr query request
     * @param collectionKey the collection key function to group documents
     * @return the collected and grouped documents
     * @throws IOException if bad things happen
     */
    private Map<String, List<SolrDocument>> collectDocuments(SolrQueryRequest req, Function<SolrDocument, String> collectionKey) throws IOException {
        String query = req.getParams().get(CommonParams.Q);
        //query and collect all documents
        return collectDocuments(query, req, collectionKey);
    }

    /**
     * Collects the document matching the given solr query request by using the given collection key function.
     *
     * @param query         the plain solr query
     * @param req           the request object
     * @param collectionKey the key to collected documents
     * @return the collected and grouped documents
     * @throws IOException if bad things happen
     */
    private Map<String, List<SolrDocument>> collectDocuments(String query, SolrQueryRequest req, Function<SolrDocument, String> collectionKey) throws IOException {
        //query and collect all documents
        Set<String> fields = getFields(req.getParams().get(CommonParams.FL));
        //we need it every time
        if (fields != null && !fields.contains(Schema.DATA)) {
            fields.add(Schema.DATA);
        }
        DocList result = docListProvider.doSimpleQuery(query, req, 0, Integer.MAX_VALUE);
        SolrDocumentList docs = docListProvider.docListToSolrDocumentList(result, req.getSearcher(), fields, null);
        return AnalysisDocumentBuilder.collect(docs, collectionKey);
    }


    /**
     * Converts the fields parameter in a set with single fields
     *
     * @param fl the fields parameter as string
     * @return a set containing the single fields split on ','
     */
    private Set<String> getFields(String fl) {
        if (fl == null) {
            return null;
        }
        String[] fields = fl.split(",");
        Set<String> returnFields = new HashSet<>();
        Collections.addAll(returnFields, fields);
        return returnFields;
    }

    /**
     * @return the description shown in apache solr
     */
    @Override
    public String getDescription() {
        return "Chronix Aggregation Request Handler";
    }
}
