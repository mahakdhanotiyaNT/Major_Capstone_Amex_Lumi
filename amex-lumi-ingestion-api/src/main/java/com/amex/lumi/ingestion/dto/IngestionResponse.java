package com.amex.lumi.ingestion.dto;

public class IngestionResponse {

    private String executionId;
    private String status;

    public IngestionResponse(String executionId, String status) {
        this.executionId = executionId;
        this.status = status;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getStatus() {
        return status;
    }
}