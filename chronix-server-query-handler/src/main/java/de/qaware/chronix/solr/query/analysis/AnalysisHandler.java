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

import com.google.common.base.Strings;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.qaware.chronix.Schema;
import de.qaware.chronix.server.ChronixPluginLoader;
import de.qaware.chronix.server.functions.*;
import de.qaware.chronix.server.functions.plugin.ChronixFunctionPlugin;
import de.qaware.chronix.server.functions.plugin.ChronixFunctions;
import de.qaware.chronix.server.types.ChronixTimeSeries;
import de.qaware.chronix.server.types.ChronixType;
import de.qaware.chronix.server.types.ChronixTypePlugin;
import de.qaware.chronix.server.types.ChronixTypes;
import de.qaware.chronix.solr.query.ChronixQueryParams;
import de.qaware.chronix.solr.query.date.DateQueryParser;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.handler.component.SearchHandler;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.SchemaField;
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
    private static final String DATA_WITH_LEADING_AND_TRAILING_COMMA = "," + Schema.DATA + ",";
    private final DocListProvider docListProvider;
    private final DateQueryParser subQueryDateRangeParser = new DateQueryParser(new String[]{ChronixQueryParams.DATE_START_FIELD, ChronixQueryParams.DATE_END_FIELD});

    private static final Injector INJECTOR = Guice.createInjector(Stage.PRODUCTION,
            ChronixPluginLoader.of(ChronixTypePlugin.class),
            ChronixPluginLoader.of(ChronixFunctionPlugin.class));

    private static final ChronixTypes TYPES = INJECTOR.getInstance(ChronixTypes.class);
    private static final ChronixFunctions FUNCTIONS = INJECTOR.getInstance(ChronixFunctions.class);

    /**
     * Constructs an isAggregation handler
     *
     * @param docListProvider - the search provider for the DocList Result
     */
    public AnalysisHandler(DocListProvider docListProvider) {
        this.docListProvider = docListProvider;
    }

    private static boolean hasMatchingAnalyses(FunctionValueMap functionValueMap) {
        if (functionValueMap == null || functionValueMap.sizeOfAnalyses() == 0) {
            return false;
        } else {
            //Analyses
            //-> return the document if the value is true
            for (int i = 0; i < functionValueMap.sizeOfAnalyses(); i++) {
                //we have found a positive analysis, lets return the document
                if (functionValueMap.getAnalysisValue(i)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * @param functionValueMap the function value map
     * @return false if the the function value map is null or if there are no transformations and aggregations
     */
    private static boolean hasTransformationsOrAggregations(FunctionValueMap functionValueMap) {
        return functionValueMap != null && functionValueMap.sizeOfTransformations() + functionValueMap.sizeOfAggregations() > 0;
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
        String[] chronixFunctions = req.getParams().getParams(ChronixQueryParams.CHRONIX_FUNCTION);
        String chronixJoin = req.getParams().get(ChronixQueryParams.CHRONIX_JOIN);

        //Do a query and collect them on the join function
        JoinFunction key = new JoinFunction(chronixJoin);
        Map<String, List<SolrDocument>> collectedDocs = collectDocuments(req, key);

        //If no rows should returned, we only return the num found
        if (rows == 0) {
            results.setNumFound(collectedDocs.keySet().size());
        } else {
            //Otherwise return the analyzed time series
            final TypeFunctions typeFunctions = QueryEvaluator.extractFunctions(chronixFunctions, TYPES, FUNCTIONS);
            final List<SolrDocument> resultDocuments = analyze(req, typeFunctions, key, collectedDocs, !JoinFunction.isDefaultJoinFunction(key));
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
     * @param req           the solr request with all information
     * @param functions     the chronix analysis that is applied
     * @param key           the key for joining documents
     * @param collectedDocs the prior collected documents of the query
     * @param isJoined      true if the documents are joined on a user defined attribute combination
     * @return a list containing the analyzed time series as solr documents
     * @throws IOException              if bad things happen in querying the documents
     * @throws IllegalArgumentException if the given analysis is not defined
     * @throws ParseException           when the start / end within the sub query could not be parsed
     */
    private List<SolrDocument> analyze(SolrQueryRequest req, TypeFunctions functions, JoinFunction key, Map<String, List<SolrDocument>> collectedDocs, boolean isJoined) throws IOException, IllegalStateException, ParseException {

        final SolrParams params = req.getParams();
        final long queryStart = Long.parseLong(params.get(ChronixQueryParams.QUERY_START_LONG));
        final long queryEnd = Long.parseLong(params.get(ChronixQueryParams.QUERY_END_LONG));

        //Check if the data field should be returned - default is true
        final String fields = params.get(CommonParams.FL, Schema.DATA);
        final boolean dataShouldReturned = fields.contains(DATA_WITH_LEADING_AND_TRAILING_COMMA);
        final boolean dataAsJson = fields.contains(ChronixQueryParams.DATA_AS_JSON);

        //the data is needed if there are functions, or the data should be returned or the data is requested as json
        boolean decompressDataAsItIsRequested = (!functions.isEmpty() || dataAsJson || dataShouldReturned);

        final List<SolrDocument> resultDocuments = Collections.synchronizedList(new ArrayList<>(collectedDocs.size()));

        collectedDocs.entrySet().parallelStream().forEach(docs -> {
            try {
                String type = evaluateType(docs);
                if (type != null) {
                    ChronixType chronixType = TYPES.getTypeForName(type);

                    if (chronixType != null) {
                        //For each type in the metric.
                        QueryFunctions typeFunctions = functions.getTypeFunctions(chronixType);
                        if (typeFunctions != null) {

                            FunctionValueMap functionValues = new FunctionValueMap(
                                    typeFunctions.sizeOfAggregations(),
                                    typeFunctions.sizeOfAnalyses(),
                                    typeFunctions.sizeOfTransformations());

                            //initialize the analysis handler
                            final ChronixTimeSeries timeSeries = chronixType.convert(
                                    docs.getValue(),
                                    queryStart, queryEnd,
                                    decompressDataAsItIsRequested);

                            //Only if we have functions, execute the following block
                            if (!functions.isEmpty()) {
                                //first we do the transformations
                                if (typeFunctions.containsTransformations()) {
                                    for (ChronixTransformation transformation : typeFunctions.getTransformations()) {
                                        timeSeries.applyTransformation(transformation, functionValues);
                                    }
                                }

                                //then we apply aggregations
                                if (typeFunctions.containsAggregations()) {
                                    for (ChronixAggregation aggregation : typeFunctions.getAggregations()) {
                                        timeSeries.applyAggregation(aggregation, functionValues);
                                    }
                                }
                                //finally the analyses
                                if (typeFunctions.containsAnalyses()) {
                                    applyFunctions(req, key, queryStart, queryEnd, docs, chronixType, typeFunctions, functionValues, timeSeries);
                                }
                            }

                            //We Return the document, if
                            // 1) the data is explicit requested as json
                            // 2) there are aggregations / transformations
                            // 3) there are matching analyses
                            if (dataAsJson || hasTransformationsOrAggregations(functionValues) || hasMatchingAnalyses(functionValues) || isJoined) {
                                //Here we have to build the document with the results of the analyses
                                resultDocuments.add(asSolrDocument(dataShouldReturned, dataAsJson, docs, functionValues, timeSeries));
                            }
                        }
                    }
                }
            } catch (ParseException | IOException e) {
                LOGGER.info("Could not parse query due to an exception", e);
            }

        });
        return resultDocuments;
    }

    private String evaluateType(Map.Entry<String, List<SolrDocument>> docs) {
        if (docs.getValue() == null || docs.getValue().isEmpty()) {
            return null;
        }

        return docs.getValue().get(0).getFieldValue(Schema.TYPE).toString();
    }

    private void applyFunctions(SolrQueryRequest req, JoinFunction key, long queryStart, long queryEnd, Map.Entry<String, List<SolrDocument>> docs, ChronixType chronixType, QueryFunctions typeFunctions, FunctionValueMap functionValues, ChronixTimeSeries timeSeries) throws ParseException, IOException {
        for (ChronixAnalysis analysis : typeFunctions.getAnalyses()) {

            if (analysis.needSubquery()) {
                //lets parse the sub-query for start and and end terms
                String modifiedSubQuery = subQueryDateRangeParser.replaceRangeQueryTerms(analysis.getSubquery());
                Map<String, List<SolrDocument>> subQueryDocuments = collectDocuments(modifiedSubQuery, req, key);

                //execute the analysis with all sub documents
                subQueryDocuments.entrySet().parallelStream().forEach(subDocs -> {
                    //Only if we have a different time series
                    if (!docs.getKey().equals(subDocs.getKey())) {

                        final ChronixTimeSeries subQueryTimeSeries = chronixType.convert(subDocs.getValue(), queryStart, queryEnd, true);
                        timeSeries.applyPairAnalysis((ChronixPairAnalysis) analysis, subQueryTimeSeries, functionValues);
                    }
                });

            } else {
                //Execute analysis and store the result
                timeSeries.applyAnalysis(analysis, functionValues);
            }
        }
    }


    private SolrDocument asSolrDocument(boolean dataShouldReturned, boolean dataAsJson, Map.Entry<String, List<SolrDocument>> docs, FunctionValueMap functionValues, ChronixTimeSeries timeSeries) {
        SolrDocument doc = new SolrDocument();

        //Add the function results
        addAnalysesAndResults(functionValues, doc);

        //add the join key
        doc.put(ChronixQueryParams.JOIN_KEY, docs.getKey());

        //add the attributes
        timeSeries.attributes().forEach(doc::addField);

        //add the metric field as it is not stored in the attributes
        doc.addField(Schema.NAME, timeSeries.getName());
        doc.addField(Schema.TYPE, timeSeries.getType());
        doc.addField(Schema.START, timeSeries.getStart());
        doc.addField(Schema.END, timeSeries.getEnd());

        if (dataShouldReturned) {
            //ensure that the returned data is sorted
            timeSeries.sort();
            //data should returned serialized as json
            if (dataAsJson) {
                doc.setField(ChronixQueryParams.DATA_AS_JSON, timeSeries.dataAsJson());
            } else {
                doc.addField(Schema.DATA, timeSeries.dataAsBlob());
            }
        }
        return doc;
    }

    /**
     * Add the functions and its results to the given solr document
     *
     * @param functionValueMap the function value map with the functions and the results
     * @param doc              the solr document to add the result
     */
    private static void addAnalysesAndResults(FunctionValueMap functionValueMap, SolrDocument doc) {

        //For identification purposes
        int counter = 0;

        //add the transformation information
        for (int transformation = 0; transformation < functionValueMap.sizeOfTransformations(); transformation++) {
            ChronixTransformation chronixTransformation = functionValueMap.getTransformation(transformation);
            doc.put(counter + "_" + ChronixQueryParams.FUNCTION + "_" + chronixTransformation.getQueryName(), chronixTransformation.getArguments());
            counter++;
        }

        //add the aggregation information
        for (int aggregation = 0; aggregation < functionValueMap.sizeOfAggregations(); aggregation++) {
            ChronixAggregation chronixAggregation = functionValueMap.getAggregation(aggregation);
            double value = functionValueMap.getAggregationValue(aggregation);
            doc.put(counter + "_" + ChronixQueryParams.FUNCTION + "_" + chronixAggregation.getQueryName(), value);

            //Only if arguments exists
            if (chronixAggregation.getArguments().length != 0) {
                doc.put(counter + "_" + ChronixQueryParams.FUNCTION_ARGUMENTS + "_" + chronixAggregation.getQueryName(), chronixAggregation.getArguments());
            }
            counter++;
        }

        //add the analyses information
        for (int analysis = 0; analysis < functionValueMap.sizeOfAnalyses(); analysis++) {
            ChronixAnalysis chronixAnalysis = functionValueMap.getAnalysis(analysis);
            boolean value = functionValueMap.getAnalysisValue(analysis);
            String identifier = functionValueMap.getAnalysisIdentifier(analysis);
            String nameWithLeadingUnderscore;

            //Check if there is an identifier
            if (Strings.isNullOrEmpty(identifier)) {
                nameWithLeadingUnderscore = "_" + chronixAnalysis.getQueryName();
            } else {
                nameWithLeadingUnderscore = "_" + chronixAnalysis.getQueryName() + "_" + identifier;
            }

            //Add some information about the analysis
            doc.put(counter + "_" + ChronixQueryParams.FUNCTION + nameWithLeadingUnderscore, value);

            //Only if arguments exists
            if (chronixAnalysis.getArguments().length != 0) {
                doc.put(counter + "_" + ChronixQueryParams.FUNCTION_ARGUMENTS + nameWithLeadingUnderscore, chronixAnalysis.getArguments());
            }
            counter++;
        }
    }


    /**
     * Collects the document matching the given solr query request by using the given collection key function.
     *
     * @param req           the solr query request
     * @param collectionKey the collection key function to group documents
     * @return the collected and grouped documents
     * @throws IOException if bad things happen
     */
    private Map<String, List<SolrDocument>> collectDocuments(SolrQueryRequest req, JoinFunction collectionKey) throws IOException {
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
    private Map<String, List<SolrDocument>> collectDocuments(String query, SolrQueryRequest req, JoinFunction collectionKey) throws IOException {
        //query and collect all documents
        Set<String> fields = getFields(req.getParams().get(CommonParams.FL), req.getSchema().getFields());

        //we always need the data field
        fields.add(Schema.DATA);

        //add the involved fields from in the join key
        if (!isEmptyArray(collectionKey.involvedFields())) {
            Collections.addAll(fields, collectionKey.involvedFields());
        }

        DocList result = docListProvider.doSimpleQuery(query, req, 0, Integer.MAX_VALUE);
        SolrDocumentList docs = docListProvider.docListToSolrDocumentList(result, req.getSearcher(), fields, null);
        return collect(docs, collectionKey);
    }

    /**
     * Collects the given document and groups them using the join function result
     *
     * @param docs         the found documents that should be grouped by the join function
     * @param joinFunction the join function
     * @return the grouped documents
     */
    private static Map<String, List<SolrDocument>> collect(SolrDocumentList docs, Function<SolrDocument, String> joinFunction) {
        Map<String, List<SolrDocument>> collectedDocs = new HashMap<>();

        for (SolrDocument doc : docs) {
            String key = joinFunction.apply(doc);

            if (!collectedDocs.containsKey(key)) {
                collectedDocs.put(key, new ArrayList<>());
            }

            collectedDocs.get(key).add(doc);
        }


        return collectedDocs;
    }

    private boolean isEmptyArray(String[] array) {
        return array == null || array.length == 0;
    }


    /**
     * Converts the fields parameter in a set with single fields
     *
     * @param fl     the fields parameter as string
     * @param schema the solr schema
     * @return a set containing the single fields split on ','
     */
    private Set<String> getFields(String fl, Map<String, SchemaField> schema) {
        if (fl == null) {
            return new HashSet<>(schema.keySet());
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
