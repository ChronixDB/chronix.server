/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.response;

import de.qaware.chronix.Schema;
import de.qaware.chronix.converter.serializer.JsonKassiopeiaSimpleSerializer;
import de.qaware.chronix.converter.serializer.ProtoBufKassiopeiaSimpleSerializer;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StoredField;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.transform.DocTransformer;
import org.apache.solr.response.transform.TransformerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

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
    /**
     * The name of the field holding the sax representation
     */
    public static final String DATA_AS_SAX = "dataAsSAX";
    public static final String SIZE = "size";

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformerFactory.class);

    private static final String DEF_PAA_SIZE = "4";
    private static final String PAA = "paa";

    private static final String DEF_ALPHABET_SIZE = "7";
    private static final String ALPHABET = "alpha";

    private static final String DEF_THRESHOLD = "0.01";
    private static final String THRESHOLD = "threshold";

    @Override
    public DocTransformer create(String field, SolrParams params, SolrQueryRequest req) {
        return new DataFieldSerializer(field, params);
    }

    /**
     * Class to transform the data field into a json representation.
     */
    private static class DataFieldSerializer extends DocTransformer {
        private final String name;
        private final int paa;
        private final int alpha;
        private final double threshold;

        /**
         * Constructs a solr document transformer for the date field.
         *
         * @param flName the name of the field
         * @param params the request params
         */
        public DataFieldSerializer(String flName, SolrParams params) {
            LOGGER.debug("Constructing Chronix transformer for field {}", flName);
            this.name = flName;
            this.paa = Integer.valueOf(params.get(PAA, DEF_PAA_SIZE));
            this.alpha = Integer.valueOf(params.get(ALPHABET, DEF_ALPHABET_SIZE));
            this.threshold = Double.valueOf(params.get(THRESHOLD, DEF_THRESHOLD));
        }

        /**
         * @return the name of the transformer
         */
        public String getName() {
            return this.name;
        }

        /**
         * Transforms the given solr document.
         * Overrides the data field with the raw json data
         *
         * @param doc   the document
         * @param docID the doc id (not used)
         * @throws UnsupportedEncodingException when the decompressed data could be correctly encoded
         */
        public void transform(SolrDocument doc, int docID) throws UnsupportedEncodingException {

            if (doc.containsKey(Schema.DATA)) {
                LOGGER.debug("Transforming data field to json. Document {}", doc);
                //we only have to decompress the field
                MetricTimeSeries timeSeries = getRawPoints(doc);
                timeSeries.sort();

                switch (this.name) {
                    case DATA_AS_JSON:
                        byte[] json = new JsonKassiopeiaSimpleSerializer().toJson(timeSeries);
                        doc.setField(DATA_AS_JSON, new String(json, "UTF-8"));
                        break;
                    case DATA_AS_SAX:
                        SAXProcessor sp = new SAXProcessor();
                        NormalAlphabet na = new NormalAlphabet();
                        try {
                            String sax = String.valueOf(sp.ts2string(timeSeries.getValues().toArray(), paa, na.getCuts(alpha), threshold));
                            doc.setField(DATA_AS_SAX, sax);
                            doc.setField(SIZE, timeSeries.size());

                        } catch (SAXException e) {
                            LOGGER.error("Could not convert time series to sax representation. Returning default", e);
                        }
                        break;
                    default:
                        LOGGER.debug("Data representation {} unknown", this.name);
                }
            }
        }

        /**
         * Gets an iterator of pairs (timestamp,value) over the time series records data.
         *
         * @param doc the solr document representing the time series record
         * @return an iterator with pairs of timestamp and value
         */
        private MetricTimeSeries getRawPoints(SolrDocument doc) {
            StoredField data = (StoredField) doc.getFieldValue(Schema.DATA);
            doc.remove(Schema.DATA);

            long tsStart = getLong(doc.getFieldValue(Schema.START));
            long tsEnd = getLong(doc.getFieldValue(Schema.END));

            MetricTimeSeries.Builder ts = new MetricTimeSeries.Builder("");
            ProtoBufKassiopeiaSimpleSerializer.from(data.binaryValue().bytes, tsStart, tsEnd, ts);
            return ts.build();
        }

        /**
         * Gets the numeric (long) representation of the fields value.
         *
         * @param field the field as object
         * @return -1 if the field value has no numeric value, otherwise the numeric value as long
         */
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
