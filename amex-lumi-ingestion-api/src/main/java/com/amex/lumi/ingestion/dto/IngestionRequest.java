package com.amex.lumi.ingestion.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class IngestionRequest {

    @NotNull(message = "File location is required")
    private MultipartFile fileLocation;

    @NotNull(message = "Control file location is required")
    private MultipartFile controlFileLocation;

    public MultipartFile getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(MultipartFile fileLocation) {
        this.fileLocation = fileLocation;
    }

    public MultipartFile getControlFileLocation() {
        return controlFileLocation;
    }

    public void setControlFileLocation(MultipartFile controlFileLocation) {
        this.controlFileLocation = controlFileLocation;
    }
}