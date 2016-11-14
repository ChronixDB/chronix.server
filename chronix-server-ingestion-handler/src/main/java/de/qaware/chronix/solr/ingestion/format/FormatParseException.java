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
package de.qaware.chronix.solr.ingestion.format;

/**
 * Is thrown if an error occured during format parsing.
 */
public class FormatParseException extends Exception {
    /**
     * Constructor.
     */
    public FormatParseException() {
    }

    /**
     * Constructor.
     *
     * @param message Message.
     */
    public FormatParseException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message Message.
     * @param cause   Cause.
     */
    public FormatParseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     *
     * @param cause Cause.
     */
    public FormatParseException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor.
     *
     * @param message            Message.
     * @param cause              Cause.
     * @param enableSuppression  whether or not suppression is enabled
     *                           or disabled
     * @param writableStackTrace whether or not the stack trace should
     *                           be writable
     */
    public FormatParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
