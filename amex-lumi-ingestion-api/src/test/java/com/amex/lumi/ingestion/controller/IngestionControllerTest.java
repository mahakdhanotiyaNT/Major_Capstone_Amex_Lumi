package com.amex.lumi.ingestion.controller;

import com.amex.lumi.ingestion.dto.IngestionResponse;
import com.amex.lumi.ingestion.exception.IngestionException;
import com.amex.lumi.ingestion.service.IngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(IngestionController.class)
class IngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IngestionService ingestionService;

    @Test
    void shouldTriggerIngestionSuccessfully() throws Exception {

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

        when(ingestionService.triggerIngestion(any()))
                .thenReturn(
                        new IngestionResponse(
                                "test-execution-id",
                                "Ingestion triggered successfully"
                        )
                );

        mockMvc.perform(
                        multipart("/api/v1/ingestions")
                                .file(inputFile)
                                .file(controlFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnBadRequestWhenControlFileIsMissing()
            throws Exception {

        MockMultipartFile inputFile =
                new MockMultipartFile(
                        "fileLocation",
                        "employees.csv",
                        "text/csv",
                        "employee_id,first_name\n1,John".getBytes()
                );

        mockMvc.perform(
                        multipart("/api/v1/ingestions")
                                .file(inputFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status").value(400)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Control file location is required")
                );
    }

    @Test
    void shouldReturnBadRequestForIngestionException()
            throws Exception {

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

        when(ingestionService.triggerIngestion(any()))
                .thenThrow(
                        new IngestionException(
                                "Unable to process uploaded file"
                        )
                );

        mockMvc.perform(
                        multipart("/api/v1/ingestions")
                                .file(inputFile)
                                .file(controlFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status").value(400)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Unable to process uploaded file")
                );
    }
}