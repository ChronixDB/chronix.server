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
