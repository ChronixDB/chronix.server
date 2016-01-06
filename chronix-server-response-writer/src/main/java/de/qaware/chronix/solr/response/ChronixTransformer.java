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
package de.qaware.chronix.solr.response;

import de.qaware.chronix.Schema;
import de.qaware.chronix.converter.common.Compression;
import de.qaware.chronix.converter.serializer.JsonKassiopeiaSimpleSerializer;
import de.qaware.chronix.converter.serializer.ProtoBufKassiopeiaSimpleSerializer;
import de.qaware.chronix.timeseries.Pair;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StoredField;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.transform.DocTransformer;
import org.apache.solr.response.transform.TransformerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Data field transformer to decompress data.
 * Overrides the data field and put the json string in it
 *
 * @author f.lautenschlager
 */
public class ChronixTransformer extends TransformerFactory {
    /**
     * The name of the field holding the raw json data
     */
    public static final String DATA_AS_JSON = "dataAsJson";

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformerFactory.class);

    @Override
    public DocTransformer create(String field, SolrParams params, SolrQueryRequest req) {
        return new DataFieldSerializer(field);
    }

    static class DataFieldSerializer extends DocTransformer {
        final String name;

        public DataFieldSerializer(String flName) {
            LOGGER.debug("Constructing chronix transformer for field {}", flName);
            this.name = flName;
        }

        /**
         * @return -the name of the transformer
         */
        public String getName() {
            return this.name;
        }

        /**
         * Transforms the given solr document.
         * Overrides the data field with the raw json data
         *
         * @param doc   - the document
         * @param docid -the doc id (not used)
         * @throws UnsupportedEncodingException when the decompressed data could be correctly encoded
         */
        public void transform(SolrDocument doc, int docid) throws UnsupportedEncodingException {

            if (doc.containsKey(Schema.DATA)) {
                LOGGER.debug("Transforming data field to json. Document {}", doc);
                //we only have to decompress the field
                Iterator<Pair> points = getRawPoints(doc);

                List<Long> timestamps = new ArrayList<>(5000);
                List<Double> values = new ArrayList<>(5000);

                while (points.hasNext()) {
                    Pair point = points.next();
                    timestamps.add(point.getTimestamp());
                    values.add(point.getValue());
                }

                JsonKassiopeiaSimpleSerializer jsonSerializer = new JsonKassiopeiaSimpleSerializer();
                byte[] json = jsonSerializer.toJson(timestamps.stream(), values.stream());

                doc.setField(DATA_AS_JSON, new String(json, "UTF-8"));
            }
        }

        private Iterator<Pair> getRawPoints(SolrDocument doc) {
            StoredField data = (StoredField) doc.getFieldValue(Schema.DATA);
            doc.remove(Schema.DATA);

            InputStream decompressed = Compression.decompressToStream(data.binaryValue().bytes);

            long tsStart = getLong(doc.getFieldValue(Schema.START));
            long tsEnd = getLong(doc.getFieldValue(Schema.END));

            return ProtoBufKassiopeiaSimpleSerializer.from(decompressed, tsStart, tsEnd);
        }

        private long getLong(Object field) {
            if (field instanceof LongField) {
                return ((LongField) field).numericValue().longValue();
            } else if (field instanceof StoredField) {
                return ((StoredField) field).numericValue().longValue();

            } else {
                return -1;
            }
        }
    }

}
