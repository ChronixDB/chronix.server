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
package de.qaware.chronix.solr.compaction;

import de.qaware.chronix.converter.BinaryTimeSeries;
import de.qaware.chronix.converter.MetricTimeSeriesConverter;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.lucene.document.Document;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.schema.IndexSchema;

import java.nio.ByteBuffer;
import java.util.UUID;

import static de.qaware.chronix.Schema.END;
import static de.qaware.chronix.Schema.START;

/**
 * Converts solr documents to and from time series.
 *
 * @author alex.christ
 */
public class ConverterService {

    private final MetricTimeSeriesConverter converter;

    /**
     * Creates a new instance.
     */
    public ConverterService() {
        converter = new MetricTimeSeriesConverter();
    }

    /**
     * Converts a time series to a solr input document.
     * <p>
     * In order to avoid write conflicts,  the resulting document does not contain
     * the attribute "_version_" and the attribute "id" is set to a random UUID.
     *
     * @param mts the time series
     * @return solr input document representing the given time series
     */
    public SolrInputDocument toInputDocument(MetricTimeSeries mts) {
        SolrInputDocument inputDocument = new SolrInputDocument();
        converter.to(mts)
                .getFields().entrySet()
                .stream()
                .filter(it -> !"_version_".equals(it.getKey()))
                .forEach(it -> inputDocument.addField(it.getKey(), it.getValue()));
        inputDocument.setField("id", UUID.randomUUID().toString());
        return inputDocument;
    }

    /**
     * Converts a solr document to a time series.
     * <p>
     * The resulting time series does not contain user defined attributes present in the solr document
     * (see {@link de.qaware.chronix.converter.common.MetricTSSchema#isUserDefined(String)}).
     *
     * @param solrDoc the solr document
     * @return time series representing the given solr documentr
     */
    public MetricTimeSeries toTimeSeries(SolrDocument solrDoc) {
        BinaryTimeSeries.Builder btsBuilder = new BinaryTimeSeries.Builder();
        solrDoc.forEach(field -> btsBuilder.field(field.getKey(), field.getValue()));
        BinaryTimeSeries bts = btsBuilder.build();
        long start = (long) solrDoc.get(START);
        long end = (long) solrDoc.get(END);
        return converter.from(bts, start, end);
    }

    /**
     * Converts a lucene document to a solr document.
     *
     * @param schema    the index schema
     * @param luceneDoc the lucene document
     * @return solr document
     */
    public SolrDocument toSolrDoc(IndexSchema schema, Document luceneDoc) {
        SolrDocument solrDoc = new SolrDocument();
        luceneDoc.forEach(it -> solrDoc.addField(it.name(), schema.getField(it.name()).getType().toObject(it)));
        for (String field : solrDoc.getFieldNames()) {
            Object value = solrDoc.getFieldValue(field);
            if (value instanceof ByteBuffer) {
                solrDoc.setField(field, ((ByteBuffer) value).array());
            }
        }
        return solrDoc;
    }

    /**
     * Converts a lucene document to a metric time series.
     *
     * @param luceneDoc the lucene document
     * @param schema    the index schema
     * @return metric time series
     */
    public MetricTimeSeries toTimeSeries(Document luceneDoc, IndexSchema schema) {
        return toTimeSeries(toSolrDoc(schema, luceneDoc));
    }

    /**
     * Copies a given metric time series.
     *
     * @param ts the time series
     * @return builder preconfigured with values from the given time series
     */
    public MetricTimeSeries.Builder copy(MetricTimeSeries ts) {
        MetricTimeSeries.Builder result = new MetricTimeSeries.Builder(ts.getMetric());
        result.start(ts.getStart());
        result.end(ts.getEnd());
        result.points(ts.getTimestamps(), ts.getValues());
        result.attributes(ts.attributes());
        return result;
    }
}
