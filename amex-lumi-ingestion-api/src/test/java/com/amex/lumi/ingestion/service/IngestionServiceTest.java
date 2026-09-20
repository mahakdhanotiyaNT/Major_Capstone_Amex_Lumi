package com.amex.lumi.ingestion.service;

import com.amex.lumi.ingestion.client.AirflowClient;
import com.amex.lumi.ingestion.dto.IngestionRequest;
import com.amex.lumi.ingestion.dto.IngestionResponse;
import com.amex.lumi.ingestion.exception.ControlFileException;
import com.amex.lumi.ingestion.exception.IngestionException;
import com.amex.lumi.ingestion.exception.InvalidInputFileException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngestionServiceTest {

    @Mock
    private AirflowClient airflowClient;

    private IngestionService createService(
            Path uploadDirectory,
            DataSize threshold
    ) {
        return new IngestionService(
                airflowClient,
                "lumi_ingestion_dag",
                uploadDirectory.toString(),
                threshold
        );
    }

    private IngestionRequest createRequest(
            String inputFileName,
            String inputContent,
            String controlFileName,
            String controlContent
    ) {
        MockMultipartFile inputFile =
                new MockMultipartFile(
                        "fileLocation",
                        inputFileName,
                        "text/plain",
                        inputContent.getBytes()
                );

        MockMultipartFile controlFile =
                new MockMultipartFile(
                        "controlFileLocation",
                        controlFileName,
                        "text/plain",
                        controlContent.getBytes()
                );

        IngestionRequest request =
                new IngestionRequest();

        request.setFileLocation(inputFile);
        request.setControlFileLocation(controlFile);

        return request;
    }

    @Test
    void shouldRejectMissingInputFile() {

        IngestionRequest request =
                new IngestionRequest();

        request.setControlFileLocation(
                new MockMultipartFile(
                        "controlFileLocation",
                        "control.properties",
                        "text/plain",
                        "record_count=1".getBytes()
                )
        );

        IngestionService service =
                createService(
                        Path.of("test-upload"),
                        DataSize.ofMegabytes(1)
                );

        InvalidInputFileException exception =
                assertThrows(
                        InvalidInputFileException.class,
                        () -> service.triggerIngestion(request)
                );

        assertEquals(
                "CSV or JSON file is required",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectEmptyInputFile() {

        IngestionRequest request =
                createRequest(
                        "employees.csv",
                        "",
                        "control.properties",
                        "record_count=0"
                );

        IngestionService service =
                createService(
                        Path.of("test-upload"),
                        DataSize.ofMegabytes(1)
                );

        InvalidInputFileException exception =
                assertThrows(
                        InvalidInputFileException.class,
                        () -> service.triggerIngestion(request)
                );

        assertEquals(
                "CSV or JSON file is required",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectMissingControlFile() {

        IngestionRequest request =
                new IngestionRequest();

        request.setFileLocation(
                new MockMultipartFile(
                        "fileLocation",
                        "employees.csv",
                        "text/csv",
                        "employee_id,first_name\nEMP001,John"
                                .getBytes()
                )
        );

        IngestionService service =
                createService(
                        Path.of("test-upload"),
                        DataSize.ofMegabytes(1)
                );

        ControlFileException exception =
                assertThrows(
                        ControlFileException.class,
                        () -> service.triggerIngestion(request)
                );

        assertEquals(
                "Control file is required",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectUnsupportedInputFile() {

        IngestionRequest request =
                createRequest(
                        "employees.txt",
                        "employee data",
                        "control.properties",
                        "record_count=1"
                );

        IngestionService service =
                createService(
                        Path.of("test-upload"),
                        DataSize.ofMegabytes(1)
                );

        InvalidInputFileException exception =
                assertThrows(
                        InvalidInputFileException.class,
                        () -> service.triggerIngestion(request)
                );

        assertEquals(
                "Only CSV and JSON files are supported",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectMissingInputFileName() {

        MockMultipartFile inputFile =
                new MockMultipartFile(
                        "fileLocation",
                        "",
                        "text/csv",
                        "employee data".getBytes()
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

        IngestionService service =
                createService(
                        Path.of("test-upload"),
                        DataSize.ofMegabytes(1)
                );

        InvalidInputFileException exception =
                assertThrows(
                        InvalidInputFileException.class,
                        () -> service.triggerIngestion(request)
                );

        assertEquals(
                "File name is required",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectInvalidControlFileExtension() {

        IngestionRequest request =
                createRequest(
                        "employees.csv",
                        "employee_id,first_name\nEMP001,John",
                        "control.txt",
                        "record_count=1"
                );

        IngestionService service =
                createService(
                        Path.of("test-upload"),
                        DataSize.ofMegabytes(1)
                );

        ControlFileException exception =
                assertThrows(
                        ControlFileException.class,
                        () -> service.triggerIngestion(request)
                );

        assertEquals(
                "Control file must be a .properties file",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectMissingControlFileName() {

        MockMultipartFile inputFile =
                new MockMultipartFile(
                        "fileLocation",
                        "employees.csv",
                        "text/csv",
                        "employee_id,first_name\nEMP001,John"
                                .getBytes()
                );

        MockMultipartFile controlFile =
                new MockMultipartFile(
                        "controlFileLocation",
                        "",
                        "text/plain",
                        "record_count=1".getBytes()
                );

        IngestionRequest request =
                new IngestionRequest();

        request.setFileLocation(inputFile);
        request.setControlFileLocation(controlFile);

        IngestionService service =
                createService(
                        Path.of("test-upload"),
                        DataSize.ofMegabytes(1)
                );

        ControlFileException exception =
                assertThrows(
                        ControlFileException.class,
                        () -> service.triggerIngestion(request)
                );

        assertEquals(
                "Control file name is required",
                exception.getMessage()
        );
    }

    @Test
    void shouldTriggerAirflowForSmallFile()
            throws Exception {

        Path uploadDirectory =
                Files.createTempDirectory(
                        "small-csv-test"
                );

        IngestionService service =
                createService(
                        uploadDirectory,
                        DataSize.ofMegabytes(10)
                );

        IngestionRequest request =
                createRequest(
                        "employees.csv",
                        "employee_id,first_name\nEMP001,John\n",
                        "control.properties",
                        "record_count=1"
                );

        IngestionResponse response =
                service.triggerIngestion(request);

        assertEquals(
                "Small file ingestion DAG triggered successfully",
                response.getStatus()
        );

        verify(airflowClient)
                .triggerDag(
                        eq("lumi_ingestion_dag"),
                        eq("/opt/airflow/input/employees.csv"),
                        eq("/opt/airflow/input/control.properties"),
                        anyString()
                );
    }

    @Test
    void shouldTriggerAirflowForSmallJsonFile()
            throws Exception {

        Path uploadDirectory =
                Files.createTempDirectory(
                        "small-json-test"
                );

        IngestionService service =
                createService(
                        uploadDirectory,
                        DataSize.ofMegabytes(10)
                );

        IngestionRequest request =
                createRequest(
                        "employees.json",
                        "[{\"employee_id\":\"EMP001\"}]",
                        "control.properties",
                        "record_count=1"
                );

        IngestionResponse response =
                service.triggerIngestion(request);

        assertEquals(
                "Small file ingestion DAG triggered successfully",
                response.getStatus()
        );

        verify(airflowClient)
                .triggerDag(
                        eq("lumi_ingestion_dag"),
                        eq("/opt/airflow/input/employees.json"),
                        eq("/opt/airflow/input/control.properties"),
                        anyString()
                );
    }

    @Test
    void shouldThrowExceptionWhenPySparkFails()
            throws Exception {

        Path uploadDirectory =
                Files.createTempDirectory(
                        "spark-failure-test"
                );

        IngestionService service =
                createService(
                        uploadDirectory,
                        DataSize.ofBytes(1)
                );

        IngestionRequest request =
                createRequest(
                        "employees.csv",
                        "employee_id,first_name\nEMP001,John\n",
                        "control.properties",
                        "record_count=1"
                );

        Process process =
                mock(Process.class);

        when(process.getInputStream())
                .thenReturn(
                        new ByteArrayInputStream(
                                new byte[0]
                        )
                );

        when(process.waitFor())
                .thenReturn(1);

        try (
                MockedConstruction<ProcessBuilder> ignored =
                        mockConstruction(
                                ProcessBuilder.class,
                                (builder, context) -> {

                                    when(builder.environment())
                                            .thenReturn(
                                                    new HashMap<>()
                                            );

                                    when(builder.directory(
                                            any(File.class)
                                    )).thenReturn(builder);

                                    when(builder.redirectErrorStream(
                                            true
                                    )).thenReturn(builder);

                                    when(builder.start())
                                            .thenReturn(process);
                                }
                        )
        ) {

            IngestionException exception =
                    assertThrows(
                            IngestionException.class,
                            () -> service.triggerIngestion(request)
                    );

            assertEquals(
                    "PySpark file splitting failed",
                    exception.getMessage()
            );
        }
    }

    @Test
    void shouldThrowExceptionWhenNoChunksAreGenerated()
            throws Exception {

        Path uploadDirectory =
                Files.createTempDirectory(
                        "no-chunks-test"
                );

        IngestionService service =
                createService(
                        uploadDirectory,
                        DataSize.ofBytes(1)
                );

        IngestionRequest request =
                createRequest(
                        "employees.csv",
                        "employee_id,first_name\nEMP001,John\n",
                        "control.properties",
                        "record_count=1"
                );

        Process process =
                mock(Process.class);

        when(process.getInputStream())
                .thenReturn(
                        new ByteArrayInputStream(
                                new byte[0]
                        )
                );

        when(process.waitFor())
                .thenReturn(0);

        try (
                MockedConstruction<ProcessBuilder> ignored =
                        mockConstruction(
                                ProcessBuilder.class,
                                (builder, context) -> {

                                    when(builder.environment())
                                            .thenReturn(
                                                    new HashMap<>()
                                            );

                                    when(builder.directory(
                                            any(File.class)
                                    )).thenReturn(builder);

                                    when(builder.redirectErrorStream(
                                            true
                                    )).thenReturn(builder);

                                    when(builder.start())
                                            .thenReturn(process);
                                }
                        )
        ) {

            IngestionException exception =
                    assertThrows(
                            IngestionException.class,
                            () -> service.triggerIngestion(request)
                    );

            assertEquals(
                    "PySpark completed but no chunk files were generated",
                    exception.getMessage()
            );
        }
    }

    @Test
    void shouldHandlePySparkIOException()
            throws Exception {

        Path uploadDirectory =
                Files.createTempDirectory(
                        "io-error-test"
                );

        IngestionService service =
                createService(
                        uploadDirectory,
                        DataSize.ofBytes(1)
                );

        IngestionRequest request =
                createRequest(
                        "employees.csv",
                        "employee_id,first_name\nEMP001,John\n",
                        "control.properties",
                        "record_count=1"
                );

        try (
                MockedConstruction<ProcessBuilder> ignored =
                        mockConstruction(
                                ProcessBuilder.class,
                                (builder, context) -> {

                                    when(builder.environment())
                                            .thenReturn(
                                                    new HashMap<>()
                                            );

                                    when(builder.directory(
                                            any(File.class)
                                    )).thenReturn(builder);

                                    when(builder.redirectErrorStream(
                                            true
                                    )).thenReturn(builder);

                                    when(builder.start())
                                            .thenThrow(
                                                    new IOException(
                                                            "Spark unavailable"
                                                    )
                                            );
                                }
                        )
        ) {

            IngestionException exception =
                    assertThrows(
                            IngestionException.class,
                            () -> service.triggerIngestion(request)
                    );

            assertEquals(
                    "Unable to process uploaded file",
                    exception.getMessage()
            );

            assertEquals(
                    "Spark unavailable",
                    exception.getCause().getMessage()
            );
        }
    }

    @Test
    void shouldHandleInterruptedException()
            throws Exception {

        Path uploadDirectory =
                Files.createTempDirectory(
                        "interrupt-test"
                );

        IngestionService service =
                createService(
                        uploadDirectory,
                        DataSize.ofBytes(1)
                );

        IngestionRequest request =
                createRequest(
                        "employees.csv",
                        "employee_id,first_name\nEMP001,John\n",
                        "control.properties",
                        "record_count=1"
                );

        Process process =
                mock(Process.class);

        when(process.getInputStream())
                .thenReturn(
                        new ByteArrayInputStream(
                                new byte[0]
                        )
                );

        when(process.waitFor())
                .thenThrow(
                        new InterruptedException(
                                "Process interrupted"
                        )
                );

        try (
                MockedConstruction<ProcessBuilder> ignored =
                        mockConstruction(
                                ProcessBuilder.class,
                                (builder, context) -> {

                                    when(builder.environment())
                                            .thenReturn(
                                                    new HashMap<>()
                                            );

                                    when(builder.directory(
                                            any(File.class)
                                    )).thenReturn(builder);

                                    when(builder.redirectErrorStream(
                                            true
                                    )).thenReturn(builder);

                                    when(builder.start())
                                            .thenReturn(process);
                                }
                        )
        ) {

            IngestionException exception =
                    assertThrows(
                            IngestionException.class,
                            () -> service.triggerIngestion(request)
                    );

            assertEquals(
                    "PySpark process was interrupted",
                    exception.getMessage()
            );

            assertTrue(
                    Thread.currentThread().isInterrupted()
            );

            Thread.interrupted();
        }
    }

    @Test
    void shouldAcceptUppercaseFileExtension()
            throws Exception {

        Path uploadDirectory =
                Files.createTempDirectory(
                        "uppercase-extension-test"
                );

        IngestionService service =
                createService(
                        uploadDirectory,
                        DataSize.ofMegabytes(10)
                );

        IngestionRequest request =
                createRequest(
                        "employees.CSV",
                        "employee_id,first_name\nEMP001,John\n",
                        "control.properties",
                        "record_count=1"
                );

        IngestionResponse response =
                service.triggerIngestion(request);

        assertEquals(
                "Small file ingestion DAG triggered successfully",
                response.getStatus()
        );

        verify(airflowClient)
                .triggerDag(
                        eq("lumi_ingestion_dag"),
                        eq("/opt/airflow/input/employees.CSV"),
                        eq("/opt/airflow/input/control.properties"),
                        anyString()
                );
    }

    @Test
    void shouldRejectNullControlFileName() {

        MockMultipartFile inputFile =
                new MockMultipartFile(
                        "fileLocation",
                        "employees.csv",
                        "text/csv",
                        "employee_id,first_name\nEMP001,John"
                                .getBytes()
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

        IngestionService service =
                createService(
                        Path.of("test-upload"),
                        DataSize.ofMegabytes(1)
                );

        ControlFileException exception =
                assertThrows(
                        ControlFileException.class,
                        () -> service.triggerIngestion(request)
                );

        assertEquals(
                "Control file name is required",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNullInputFileName() {

        MockMultipartFile inputFile =
                new MockMultipartFile(
                        "fileLocation",
                        null,
                        "text/csv",
                        "employee_id,first_name\nEMP001,John"
                                .getBytes()
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

        IngestionService service =
                createService(
                        Path.of("test-upload"),
                        DataSize.ofMegabytes(1)
                );

        InvalidInputFileException exception =
                assertThrows(
                        InvalidInputFileException.class,
                        () -> service.triggerIngestion(request)
                );

        assertEquals(
                "File name is required",
                exception.getMessage()
        );
    }
}