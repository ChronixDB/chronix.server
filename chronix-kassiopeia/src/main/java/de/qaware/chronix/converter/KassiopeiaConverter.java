/*
 *    Copyright (C) 2015 QAware GmbH
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
package de.qaware.chronix.converter;


import de.qaware.chronix.converter.dt.ProtocolBuffers;
import de.qaware.chronix.dts.Pair;
import de.qaware.chronix.timeseries.TimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Iterator;

/**
 * Kassiopeia converter to convert our time series into a binary storage document and back
 *
 * @author f.lautenschlager
 */
public class KassiopeiaConverter implements DocumentConverter<TimeSeries<Long, Double>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KassiopeiaConverter.class);

    @Override
    public TimeSeries<Long, Double> from(BinaryStorageDocument binaryStorageDocument, long queryStart, long queryEnd) {

        //first decompress
        InputStream decompressedBytes = Compression.decompress(binaryStorageDocument.getData());

        //second deserialize the points
        Iterator<Pair<Long, Double>> points = ProtocolBuffersConverter.from(decompressedBytes, binaryStorageDocument.getStart(), binaryStorageDocument.getEnd(), queryStart, queryEnd);

        TimeSeries<Long, Double> timeSeries = new TimeSeries<>(points);

        //fourth add attributes
        binaryStorageDocument.getFields().forEach((attribute, value) -> {

            if (Schema.isUserDefined(attribute)) {
                timeSeries.addAttribute(attribute, value);
            }
        });

        return timeSeries;

    }


    @Override
    public BinaryStorageDocument to(TimeSeries<Long, Double> document) {

        //for the case, that someone tries to store an empty time series
        byte[] compressed = new byte[]{};
        long start = 0;
        long end = 0;

        //-oo is represented through the first element that is null, hence if only have one document the time series is empty
        if (document.size() == 1) {
            LOGGER.info("Empty time series detected.");
        } else {
            //start of time series
            start = document.get(1).getFirst();
            end = document.get(document.size() - 1).getFirst();

            //first serialize the data
            ProtocolBuffers.NumericPoints serializedDataProto = ProtocolBuffersConverter.to(document.iterator());
            byte[] bytes = serializedDataProto.toByteArray();

            //then compress the data
            compressed = Compression.compress(bytes);
        }

        //Create a builder with the minimal required fields
        BinaryStorageDocument.Builder builder = new BinaryStorageDocument.Builder()
                .data(compressed)
                .start(start)
                .end(end);

        //add the attributes to the binary storage document
        document.getAttributes().forEachRemaining(entry -> {
            builder.field(entry.getKey(), entry.getValue());
        });

        return builder.build();

    }
}
