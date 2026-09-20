package com.amex.lumi.ingestion.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IngestionResponseTest {

    @Test
    void shouldCreateResponseAndReturnValues() {

        IngestionResponse response =
                new IngestionResponse(
                        "execution-123",
                        "STARTED"
                );

        assertEquals(
                "execution-123",
                response.getExecutionId()
        );

        assertEquals(
                "STARTED",
                response.getStatus()
        );
    }
}