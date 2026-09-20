package com.amex.lumi.ingestion.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorResponseTest {

    @Test
    void shouldCreateErrorResponseAndReturnValues() {

        ErrorResponse response =
                new ErrorResponse(
                        400,
                        "File location is required"
                );

        assertEquals(
                400,
                response.getStatus()
        );

        assertEquals(
                "File location is required",
                response.getMessage()
        );
    }
}