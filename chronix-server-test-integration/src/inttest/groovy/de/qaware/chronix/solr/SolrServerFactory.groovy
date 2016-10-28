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
package de.qaware.chronix.solr

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer
import org.apache.solr.core.CoreContainer

/**
 * Creates an embedded solr server configured against the test core.
 *
 * @author alex.christ
 */
class SolrServerFactory {
    private static final String SOLR_HOME = new File(SolrServerFactory.getResource('.').toURI()).path
    private static final String CORE_NAME = 'chronix'

    /**
     * @return embedded solr server
     */
    public static EmbeddedSolrServer newEmbeddedSolrServer() {
        System.setProperty('solr.solr.home', SOLR_HOME)
        CoreContainer container = new CoreContainer()
        container.load()
        new EmbeddedSolrServer(container, CORE_NAME)
    }
}