package com.amex.lumi.ingestion.exception;

/**

 Thrown when the expected record count does not match the actual loaded count
 */
public class RecordCountMismatchException extends IngestionException {

    public RecordCountMismatchException(String message) {
        super(message);
    }
}
