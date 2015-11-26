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
package de.qaware.chronix.analysis.aggregation;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.*;

/**
 * The QParser Plugin for aggregation functions
 *
 * @author f.lautenschlager
 */
public class AggregationPlugin extends QParserPlugin {

    private static final java.lang.String QUERY_START_LONG = "query_start_long";
    private static final java.lang.String QUERY_END_LONG = "query_end_long";

    private static final String AGGREGATION_PARAMETER = "ag";

    @Override
    public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        return new QParser(qstr, localParams, params, req) {

            @Override
            public Query parse() throws SyntaxError {
                final String aggregation = localParams.get(AGGREGATION_PARAMETER, "");

                if (aggregation.isEmpty()) {
                    throw new SyntaxError("Function Query aggregation must at least contain the parameter '" + AGGREGATION_PARAMETER + "'");
                }

                long queryStart = Long.valueOf(params.get(QUERY_START_LONG, "-1"));
                long queryEnd = Long.valueOf(params.get(QUERY_END_LONG, "-1"));


                return new AnalyticsQuery() {
                    @Override
                    public DelegatingCollector getAnalyticsCollector(ResponseBuilder rb, IndexSearcher searcher) {
                        return new AggregationCollector(rb, aggregation, queryStart, queryEnd);
                    }
                };
            }
        };
    }

    @Override
    public void init(NamedList args) {
        //we will see...
    }

}
