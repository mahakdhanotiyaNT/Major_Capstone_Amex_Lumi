package com.amex.lumi.ingestion.exception;

import com.amex.lumi.ingestion.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**

  Handles application exceptions and converts them into consistent API responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**

      Handles validation errors from request DTO validation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception) {

        String message = exception.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErrorResponse response =
                new ErrorResponse(status.value(), message);

        return ResponseEntity
                .status(status)
                .body(response);
    }

    /**

      Handles application-specific ingestion errors.
     */
    @ExceptionHandler(IngestionException.class)
    public ResponseEntity<ErrorResponse> handleIngestionException(
            IngestionException exception) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErrorResponse response =
                new ErrorResponse(
                        status.value(),
                        exception.getMessage()
                );

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeNotFoundException(
            EmployeeNotFoundException exception) {

        HttpStatus status = HttpStatus.NOT_FOUND;

        ErrorResponse response =
                new ErrorResponse(
                        status.value(),
                        exception.getMessage()
                );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}

