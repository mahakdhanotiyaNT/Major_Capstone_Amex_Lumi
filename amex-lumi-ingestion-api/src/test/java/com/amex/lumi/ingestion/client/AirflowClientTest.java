package com.amex.lumi.ingestion.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AirflowClientTest {

    private AirflowClient airflowClient;

    private RestClient restClient;
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    private RestClient.RequestBodySpec requestBodySpec;
    private RestClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {

        airflowClient =
                new AirflowClient(
                        "http://localhost:8082",
                        "test-user",
                        "test-password"
                );

        restClient = mock(RestClient.class);
        requestBodyUriSpec =
                mock(RestClient.RequestBodyUriSpec.class);
        requestBodySpec =
                mock(RestClient.RequestBodySpec.class);
        responseSpec =
                mock(RestClient.ResponseSpec.class);

        ReflectionTestUtils.setField(
                airflowClient,
                "restClient",
                restClient
        );

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);

        when(requestBodyUriSpec.uri(
                anyString(),
                any(Object[].class)
        )).thenReturn(requestBodySpec);

        when(requestBodySpec.contentType(any()))
                .thenReturn(requestBodySpec);

        when(requestBodySpec.headers(any()))
                .thenReturn(requestBodySpec);

        when(requestBodySpec.body(anyMap()))
                .thenReturn(requestBodySpec);

        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.toBodilessEntity())
                .thenReturn(null);
    }

    @Test
    void shouldTriggerDagForSmallFile() {

        airflowClient.triggerDag(
                "lumi_ingestion_dag",
                "/opt/airflow/input/employees.json",
                "/opt/airflow/input/control.properties",
                "execution-001"
        );

        verify(restClient).post();
        verify(requestBodyUriSpec).uri(
                anyString(),
                any(Object[].class)
        );
        verify(requestBodySpec).retrieve();
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    void shouldTriggerDagWithChunks() {

        airflowClient.triggerDagWithChunks(
                "lumi_ingestion_dag",
                List.of(
                        "/opt/airflow/input/part-1.json",
                        "/opt/airflow/input/part-2.json"
                ),
                "/opt/airflow/input/control.properties",
                "execution-002"
        );

        verify(restClient).post();
        verify(requestBodyUriSpec).uri(
                anyString(),
                any(Object[].class)
        );
        verify(requestBodySpec).retrieve();
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    void shouldPropagateExceptionWhenAirflowTriggerFails() {

        RuntimeException exception =
                new RuntimeException("Airflow unavailable");

        when(responseSpec.toBodilessEntity())
                .thenThrow(exception);

        assertThrows(
                RuntimeException.class,
                () -> airflowClient.triggerDag(
                        "lumi_ingestion_dag",
                        "/opt/airflow/input/employees.json",
                        "/opt/airflow/input/control.properties",
                        "execution-003"
                )
        );
    }
}