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
package de.qaware.chronix.solr.test.converter;

import de.qaware.chronix.converter.BinaryStorageDocument;
import de.qaware.chronix.converter.DocumentConverter;

/**
 * The default document converter, that does nothing.
 * Only for test purposes.
 *
 * @author f.lautenschlager
 */
public class DefaultDocumentConverter implements DocumentConverter<BinaryStorageDocument> {


    @Override
    public BinaryStorageDocument from(BinaryStorageDocument binaryStorageDocument, long queryStart, long queryEnd) {
        return binaryStorageDocument;
    }

    @Override
    public BinaryStorageDocument to(BinaryStorageDocument timeSeries) {
        return timeSeries;
    }
}

