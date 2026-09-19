package com.amex.lumi.ingestion.exception;

/**

  Thrown when an employee is not found for the given employee ID
 */
public class EmployeeNotFoundException extends IngestionException {

    public EmployeeNotFoundException(String message) {
        super(message);
    }
}