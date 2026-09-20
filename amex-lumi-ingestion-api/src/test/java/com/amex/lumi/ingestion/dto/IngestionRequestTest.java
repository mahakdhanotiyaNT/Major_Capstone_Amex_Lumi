package com.amex.lumi.ingestion.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IngestionRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void shouldSetAndGetFiles() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "fileLocation",
                        "employees.csv",
                        "text/csv",
                        "data".getBytes()
                );

        MockMultipartFile controlFile =
                new MockMultipartFile(
                        "controlFileLocation",
                        "control.properties",
                        "text/plain",
                        "record_count=1".getBytes()
                );

        IngestionRequest request =
                new IngestionRequest();

        request.setFileLocation(file);
        request.setControlFileLocation(controlFile);

        assertEquals(
                file,
                request.getFileLocation()
        );

        assertEquals(
                controlFile,
                request.getControlFileLocation()
        );
    }

    @Test
    void shouldRejectMissingFileLocation() {

        IngestionRequest request =
                new IngestionRequest();

        MockMultipartFile controlFile =
                new MockMultipartFile(
                        "controlFileLocation",
                        "control.properties",
                        "text/plain",
                        "record_count=1".getBytes()
                );

        request.setControlFileLocation(controlFile);

        Set<jakarta.validation.ConstraintViolation<IngestionRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "File location is required",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    void shouldRejectMissingControlFileLocation() {

        IngestionRequest request =
                new IngestionRequest();

        MockMultipartFile file =
                new MockMultipartFile(
                        "fileLocation",
                        "employees.csv",
                        "text/csv",
                        "data".getBytes()
                );

        request.setFileLocation(file);

        Set<jakarta.validation.ConstraintViolation<IngestionRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "Control file location is required",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    void shouldAcceptValidRequest() {

        IngestionRequest request =
                new IngestionRequest();

        request.setFileLocation(
                new MockMultipartFile(
                        "fileLocation",
                        "employees.csv",
                        "text/csv",
                        "data".getBytes()
                )
        );

        request.setControlFileLocation(
                new MockMultipartFile(
                        "controlFileLocation",
                        "control.properties",
                        "text/plain",
                        "record_count=1".getBytes()
                )
        );

        Set<jakarta.validation.ConstraintViolation<IngestionRequest>> violations =
                validator.validate(request);

        assertEquals(0, violations.size());
    }
}