package com.amex.lumi.ingestion.exception;

import com.amex.lumi.ingestion.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void shouldHandleValidationException() {

        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult =
                mock(BindingResult.class);

        FieldError fieldError =
                new FieldError(
                        "request",
                        "fileLocation",
                        "File location is required"
                );

        when(exception.getBindingResult())
                .thenReturn(bindingResult);

        when(bindingResult.getFieldError())
                .thenReturn(fieldError);

        ResponseEntity<ErrorResponse> response =
                handler.handleValidationException(exception);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertEquals(
                400,
                response.getBody().getStatus()
        );

        assertEquals(
                "File location is required",
                response.getBody().getMessage()
        );
    }

    @Test
    void shouldHandleIngestionException() {

        IngestionException exception =
                new IngestionException(
                        "Invalid input file"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleIngestionException(exception);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertEquals(
                400,
                response.getBody().getStatus()
        );

        assertEquals(
                "Invalid input file",
                response.getBody().getMessage()
        );
    }

    @Test
    void shouldHandleEmployeeNotFoundException() {

        EmployeeNotFoundException exception =
                new EmployeeNotFoundException(
                        "Employee not found with ID: EMP1001"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleEmployeeNotFoundException(exception);

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertEquals(
                404,
                response.getBody().getStatus()
        );

        assertEquals(
                "Employee not found with ID: EMP1001",
                response.getBody().getMessage()
        );
    }
}