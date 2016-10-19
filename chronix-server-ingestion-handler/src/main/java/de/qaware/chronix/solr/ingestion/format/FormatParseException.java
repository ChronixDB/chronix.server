/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.ingestion.format;

/**
 * Is thrown if an error occured during format parsing.
 */
public class FormatParseException extends Exception {
    public FormatParseException() {
    }

    public FormatParseException(String message) {
        super(message);
    }

    public FormatParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public FormatParseException(Throwable cause) {
        super(cause);
    }

    public FormatParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
