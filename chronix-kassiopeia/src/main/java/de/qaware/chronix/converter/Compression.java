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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Class that provides a simple gzip compression and decompression
 *
 * @author f.lautenschlager
 */
public class Compression {

    private static final Logger LOGGER = LoggerFactory.getLogger(Compression.class);

    private Compression() {
        //avoid instances
    }

    /**
     * Compresses the given byte[]
     *
     * @param decompressed - the byte[] to compress
     * @return the byte[] compressed
     */
    public static byte[] compress(byte[] decompressed) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream gzos = null;
        try {
            gzos = new GZIPOutputStream(baos);
            gzos.write(decompressed);
            gzos.flush();
            baos.flush();
        } catch (Exception e) {
            LOGGER.error("Exception occurred while compressing gzip stream.", e);
            return null;
        } finally {
            IOUtils.closeQuietly(gzos);
            IOUtils.closeQuietly(baos);
        }

        return baos.toByteArray();
    }


    /**
     * Decompressed the given byte[]
     *
     * @param compressed - the compressed byte[]
     * @return an input stream of the uncompressed byte[]
     */
    public static InputStream decompress(byte[] compressed) {

        try {
            return new GZIPInputStream(new ByteArrayInputStream(compressed));
        } catch (Exception e) {
            LOGGER.error("Exception occurred while decompressing gzip stream.", e);
        }
        return null;
    }
}
