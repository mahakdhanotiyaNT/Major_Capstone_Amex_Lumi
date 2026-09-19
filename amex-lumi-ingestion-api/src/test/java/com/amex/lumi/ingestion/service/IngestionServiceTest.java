package com.amex.lumi.ingestion.service;

import com.amex.lumi.ingestion.client.AirflowClient;
import com.amex.lumi.ingestion.dto.IngestionRequest;
import com.amex.lumi.ingestion.dto.IngestionResponse;
import com.amex.lumi.ingestion.exception.ControlFileException;
import com.amex.lumi.ingestion.exception.InvalidInputFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionServiceTest {

    @Mock
    private AirflowClient airflowClient;

    private IngestionService ingestionService;

    private Path testUploadDirectory;

    @BeforeEach
    void setUp() throws Exception {

        testUploadDirectory =
                Files.createTempDirectory("ingestion-test");

        ingestionService =
                new IngestionService(
                        airflowClient,
                        "lumi_ingestion_dag",
                        testUploadDirectory.toString(),
                        DataSize.ofMegabytes(10)
                );
    }

    @Test
    void shouldRejectMissingInputFile() {

        IngestionRequest request =
                new IngestionRequest();

        MockMultipartFile controlFile =
                new MockMultipartFile(
                        "controlFileLocation",
                        "control.properties",
                        "text/plain",
                        "record_count=10".getBytes()
                );

        request.setControlFileLocation(controlFile);

        assertThrows(
                InvalidInputFileException.class,
                () -> ingestionService.triggerIngestion(request)
        );

        verifyNoInteractions(airflowClient);
    }

    @Test
    void shouldRejectEmptyInputFile() {

        MockMultipartFile inputFile =
                new MockMultipartFile(
                        "fileLocation",
                        "employees.csv",
                        "text/csv",
                        new byte[0]
                );

        MockMultipartFile controlFile =
                new MockMultipartFile(
                        "controlFileLocation",
                        "control.properties",
                        "text/plain",
                        "record_count=0".getBytes()
                );

        IngestionRequest request =
                new IngestionRequest();

        request.setFileLocation(inputFile);
        request.setControlFileLocation(controlFile);

        assertThrows(
                InvalidInputFileException.class,
                () -> ingestionService.triggerIngestion(request)
        );

        verifyNoInteractions(airflowClient);
    }

    @Test
    void shouldRejectMissingControlFile() {

        IngestionRequest request =
                new IngestionRequest();

        MockMultipartFile inputFile =
                new MockMultipartFile(
                        "fileLocation",
                        "employees.csv",
                        "text/csv",
                        "employee_id,first_name\n1,John".getBytes()
                );

        request.setFileLocation(inputFile);

        assertThrows(
                ControlFileException.class,
                () -> ingestionService.triggerIngestion(request)
        );

        verifyNoInteractions(airflowClient);
    }

    @Test
    void shouldRejectUnsupportedInputFile() {

        MockMultipartFile inputFile =
                new MockMultipartFile(
                        "fileLocation",
                        "employees.txt",
                        "text/plain",
                        "test data".getBytes()
                );

        MockMultipartFile controlFile =
                new MockMultipartFile(
                        "controlFileLocation",
                        "control.properties",
                        "text/plain",
                        "record_count=10".getBytes()
                );

        IngestionRequest request =
                new IngestionRequest();

        request.setFileLocation(inputFile);
        request.setControlFileLocation(controlFile);

        assertThrows(
                InvalidInputFileException.class,
                () -> ingestionService.triggerIngestion(request)
        );

        verifyNoInteractions(airflowClient);
    }

    @Test
    void shouldRejectMissingInputFileName() {

        MockMultipartFile inputFile =
                new MockMultipartFile(
                        "fileLocation",
                        null,
                        "text/csv",
                        "employee_id,first_name\n1,John".getBytes()
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

        request.setFileLocation(inputFile);
        request.setControlFileLocation(controlFile);

        assertThrows(
                InvalidInputFileException.class,
                () -> ingestionService.triggerIngestion(request)
        );

        verifyNoInteractions(airflowClient);
    }

    @Test
    void shouldRejectInvalidControlFileExtension() {

        MockMultipartFile inputFile =
                new MockMultipartFile(
                        "fileLocation",
                        "employees.csv",
                        "text/csv",
                        "employee_id,first_name\n1,John".getBytes()
                );

        MockMultipartFile controlFile =
                new MockMultipartFile(
                        "controlFileLocation",
                        "control.txt",
                        "text/plain",
                        "record_count=10".getBytes()
                );

        IngestionRequest request =
                new IngestionRequest();

        request.setFileLocation(inputFile);
        request.setControlFileLocation(controlFile);

        assertThrows(
                ControlFileException.class,
                () -> ingestionService.triggerIngestion(request)
        );

        verifyNoInteractions(airflowClient);
    }

    @Test
    void shouldRejectMissingControlFileName() {

        MockMultipartFile inputFile =
                new MockMultipartFile(
                        "fileLocation",
                        "employees.csv",
                        "text/csv",
                        "employee_id,first_name\n1,John".getBytes()
                );

        MockMultipartFile controlFile =
                new MockMultipartFile(
                        "controlFileLocation",
                        null,
                        "text/plain",
                        "record_count=1".getBytes()
                );

        IngestionRequest request =
                new IngestionRequest();

        request.setFileLocation(inputFile);
        request.setControlFileLocation(controlFile);

        assertThrows(
                ControlFileException.class,
                () -> ingestionService.triggerIngestion(request)
        );

        verifyNoInteractions(airflowClient);
    }

    @Test
    void shouldTriggerAirflowForSmallFile() {

        MockMultipartFile inputFile =
                new MockMultipartFile(
                        "fileLocation",
                        "employees.csv",
                        "text/csv",
                        "employee_id,first_name\n1,John".getBytes()
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

        request.setFileLocation(inputFile);
        request.setControlFileLocation(controlFile);

        IngestionResponse response =
                ingestionService.triggerIngestion(request);

        assertNotNull(response);

        verify(
                airflowClient,
                times(1)
        ).triggerDag(
                eq("lumi_ingestion_dag"),
                eq("/opt/airflow/input/employees.csv"),
                eq("/opt/airflow/input/control.properties"),
                anyString()
        );

        verify(
                airflowClient,
                never()
        ).triggerDagWithChunks(
                anyString(),
                anyList(),
                anyString(),
                anyString()
        );
    }

    @Test
    void shouldTriggerAirflowForSmallJsonFile() {

        MockMultipartFile inputFile =
                new MockMultipartFile(
                        "fileLocation",
                        "employees.json",
                        "application/json",
                        "{\"employee_id\":\"EMP1001\"}".getBytes()
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

        request.setFileLocation(inputFile);
        request.setControlFileLocation(controlFile);

        IngestionResponse response =
                ingestionService.triggerIngestion(request);

        assertNotNull(response);

        verify(
                airflowClient,
                times(1)
        ).triggerDag(
                eq("lumi_ingestion_dag"),
                eq("/opt/airflow/input/employees.json"),
                eq("/opt/airflow/input/control.properties"),
                anyString()
        );
    }
}