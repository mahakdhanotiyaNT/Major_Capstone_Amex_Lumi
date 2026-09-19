package com.amex.lumi.ingestion.exception;

/**

  Base exception for ingestion-related application errors.
 */
public class IngestionException extends RuntimeException {

    public IngestionException(String message) {
        super(message);
    }

    public IngestionException(String message, Throwable cause) {
        super(message, cause);
    }
}