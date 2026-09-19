package com.amex.lumi.ingestion.exception;

/**

  Thrown when the uploaded input file is invalid or unsupported.
 */
public class InvalidInputFileException extends IngestionException {

    public InvalidInputFileException(String message) {
        super(message);
    }
}