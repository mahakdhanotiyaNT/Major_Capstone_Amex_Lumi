package com.amex.lumi.ingestion.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AirflowClient {

    private static final Logger log =
            LoggerFactory.getLogger(AirflowClient.class);

    private final RestClient restClient;

    private final String airflowUsername;
    private final String airflowPassword;

    public AirflowClient(
            @Value("${airflow.base-url}") String airflowBaseUrl,
            @Value("${airflow.username}") String airflowUsername,
            @Value("${airflow.password}") String airflowPassword
    ) {

        this.airflowUsername = airflowUsername;
        this.airflowPassword = airflowPassword;

        this.restClient = RestClient.builder()
                .baseUrl(airflowBaseUrl)
                .build();
    }

    /*
    Trigger DAG for small file.
     */

    public void triggerDag(
            String dagId,
            String fileLocation,
            String controlFileLocation,
            String executionId
    ) {

        log.info(
                "Triggering Airflow DAG. Execution ID: {}",
                executionId
        );

        Map<String, Object> requestBody =
                Map.of(
                        "conf",
                        Map.of(
                                "file_location",
                                fileLocation,

                                "control_file_location",
                                controlFileLocation,

                                "execution_id",
                                executionId
                        )
                );

        sendDagTrigger(
                dagId,
                executionId,
                requestBody
        );
    }

    /*
    Trigger DAG for large file chunks.
     */

    public void triggerDagWithChunks(
            String dagId,
            List<String> fileLocations,
            String controlFileLocation,
            String executionId
    ) {

        log.info(
                "Triggering Airflow DAG with {} chunk files. Execution ID: {}",
                fileLocations.size(),
                executionId
        );

        Map<String, Object> requestBody =
                Map.of(
                        "conf",
                        Map.of(
                                "file_locations",
                                fileLocations,

                                "control_file_location",
                                controlFileLocation,

                                "execution_id",
                                executionId
                        )
                );

        sendDagTrigger(
                dagId,
                executionId,
                requestBody
        );
    }

    /*
    Common Airflow trigger method.
     */

    private void sendDagTrigger(
            String dagId,
            String executionId,
            Map<String, Object> requestBody
    ) {

        try {

            restClient.post()
                    .uri(
                            "/api/v1/dags/{dagId}/dagRuns",
                            dagId
                    )
                    .contentType(
                            MediaType.APPLICATION_JSON
                    )
                    .headers(headers ->
                            headers.setBasicAuth(
                                    airflowUsername,
                                    airflowPassword
                            )
                    )
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();

            log.info(
                    "Airflow DAG triggered successfully. Execution ID: {}",
                    executionId
            );

        } catch (Exception e) {

            log.error(
                    "Failed to trigger Airflow DAG. Execution ID: {}",
                    executionId,
                    e
            );

            throw e;
        }
    }
}