package com.amex.lumi.ingestion.exception;

/**

  Thrown when the control file is missing, invalid, or cannot be processed.
 */
public class ControlFileException extends IngestionException {

    public ControlFileException(String message) {
        super(message);
    }

    public ControlFileException(String message, Throwable cause) {
        super(message, cause);
    }
}