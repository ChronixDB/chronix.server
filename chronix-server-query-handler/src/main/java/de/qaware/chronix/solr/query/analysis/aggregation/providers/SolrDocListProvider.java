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
package de.qaware.chronix.solr.query.analysis.aggregation.providers;

import de.qaware.chronix.solr.query.analysis.aggregation.DocListProvider;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.DocList;
import org.apache.solr.util.SolrPluginUtils;

import java.io.IOException;

/**
 * Solr DocList provider implementation
 *
 * @author f.lautenschlager
 */
public class SolrDocListProvider implements DocListProvider {
    @Override
    public DocList doSimpleQuery(String q, SolrQueryRequest req, int start, int limit) throws IOException {
        return SolrPluginUtils.doSimpleQuery(q, req, start, limit);
    }
}
