package com.amex.lumi.ingestion.service;
import com.amex.lumi.ingestion.client.AirflowClient;
import com.amex.lumi.ingestion.dto.IngestionRequest;
import com.amex.lumi.ingestion.dto.IngestionResponse;
import com.amex.lumi.ingestion.exception.ControlFileException;
import com.amex.lumi.ingestion.exception.IngestionException;
import com.amex.lumi.ingestion.exception.InvalidInputFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
/**
 * Handles employee file ingestion requests and coordinates
 * file processing between Spring Boot, PySpark, and Airflow.
 */
@Service
public class IngestionService {
    private static final Logger log =
            LoggerFactory.getLogger(IngestionService.class);
    private final AirflowClient airflowClient;
    private final String airflowDagId;
    private final String uploadDirectory;
    private final DataSize largeFileThreshold;
    public IngestionService(
            AirflowClient airflowClient,
            @Value("${airflow.dag-id}") String airflowDagId,
            @Value("${ingestion.upload-directory}") String uploadDirectory,
            @Value("${ingestion.large-file-threshold}") DataSize largeFileThreshold
    ) {
        this.airflowClient = airflowClient;
        this.airflowDagId = airflowDagId;
        this.uploadDirectory = uploadDirectory;
        this.largeFileThreshold = largeFileThreshold;
    }
    /**
     * Validates uploaded files, determines the processing strategy,
     * and triggers the appropriate Airflow ingestion flow.
     *
     * @param request contains the input and control files
     * @return response containing the execution ID and processing status
     */
    public IngestionResponse triggerIngestion(
            IngestionRequest request
    ) {

        log.info("Ingestion request received");
        MultipartFile file =
                request.getFileLocation();
        if (file == null || file.isEmpty()) {
            throw new InvalidInputFileException(
                    "CSV or JSON file is required"
            );
        }

        /**
        *  Control file contains the expected record count for final validation.
         */
        MultipartFile controlFile =
                request.getControlFileLocation();
        if (controlFile == null || controlFile.isEmpty()) {
            throw new ControlFileException(
                    "Control file is required"
            );
        }
        String originalFileName =
                file.getOriginalFilename();
        if (originalFileName == null
                || originalFileName.isBlank()) {
            throw new InvalidInputFileException(
                    "File name is required"
            );
        }
        String fileName =
                Paths.get(originalFileName)
                        .getFileName()
                        .toString();
        String lowerCaseFileName =
                fileName.toLowerCase();
        if (!lowerCaseFileName.endsWith(".csv")
                && !lowerCaseFileName.endsWith(".json")) {
            throw new InvalidInputFileException(
                    "Only CSV and JSON files are supported"
            );
        }
        String controlFileOriginalName =
                controlFile.getOriginalFilename();
        if (controlFileOriginalName == null
                || controlFileOriginalName.isBlank()) {
            throw new ControlFileException(
                    "Control file name is required"
            );
        }
        String controlFileName =
                Paths.get(controlFileOriginalName)
                        .getFileName()
                        .toString();
        if (!controlFileName
                .toLowerCase()
                .endsWith(".properties")) {
            throw new ControlFileException(
                    "Control file must be a .properties file"
            );
        }
        String executionId =
                UUID.randomUUID().toString();
        try {
            Path uploadPath =
                    Paths.get(uploadDirectory);
            Files.createDirectories(uploadPath);
            Path destination =
                    uploadPath.resolve(fileName);
            file.transferTo(destination);
            log.info(
                    "File uploaded successfully: {}",
                    fileName
            );
            Path controlFileDestination =
                    uploadPath.resolve(controlFileName);
            controlFile.transferTo(
                    controlFileDestination
            );
            log.info(
                    "Control file uploaded successfully: {}",
                    controlFileName
            );
            String airflowControlFileLocation =
                    "/opt/airflow/input/"
                            + controlFileName;
            log.info(
                    "Airflow control file location: {}",
                    airflowControlFileLocation
            );
            long fileSize =
                    Files.size(destination);
            long thresholdSize =
                    largeFileThreshold.toBytes();
            log.info(
                    "Uploaded file size: {} bytes",
                    fileSize
            );
            log.info(
                    "Configured large-file threshold: {} bytes",
                    thresholdSize
            );

             /**
             *  File size determines whether the file is processed directly or split using Pyspark.
             */
            boolean isLargeFile =
                    fileSize > thresholdSize;
            if (!isLargeFile) {
                log.info(
                        "Small file detected. Sending directly to Airflow."
                );
                String airflowFileLocation =
                        "/opt/airflow/input/"
                                + fileName;
                airflowClient.triggerDag(
                        airflowDagId,
                        airflowFileLocation,
                        airflowControlFileLocation,
                        executionId
                );
                return new IngestionResponse(
                        executionId,
                        "Small file ingestion DAG triggered successfully"
                );
            }
            log.info(
                    "Large file detected. Starting PySpark file splitting."
            );
            Path splitOutputDirectory =
                    uploadPath
                            .resolve("phase2_split")
                            .resolve(executionId);
            Files.createDirectories(
                    splitOutputDirectory
            );
            String format =
                    lowerCaseFileName.endsWith(".csv")
                            ? "csv"
                            : "json";
            Path splitScript =
                    Paths.get(
                                    System.getProperty("user.dir")
                            )
                            .resolve("pyspark")
                            .resolve("split_file.py");
            log.info(
                    "Starting PySpark with script: {}",
                    splitScript
            );
            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            "C:\\Spark\\spark-3.5.6-bin-hadoop3-scala2.13\\bin\\spark-submit.cmd",
                            "--master",
                            "local[*]",
                            splitScript.toString(),
                            "--input",
                            destination.toString(),
                            "--output",
                            splitOutputDirectory.toString(),
                            "--format",
                            format
                    );
            processBuilder.environment().put(
                    "HADOOP_HOME",
                    "C:\\hadoop"
            );
            processBuilder.environment().put(
                    "PYSPARK_PYTHON",
                    "C:\\Users\\Mahak Dhanotiya\\AppData\\Local\\Programs\\Python\\Python310\\python.exe"
            );
            processBuilder.environment().put(
                    "PYSPARK_DRIVER_PYTHON",
                    "C:\\Users\\Mahak Dhanotiya\\AppData\\Local\\Programs\\Python\\Python310\\python.exe"
            );
            processBuilder.environment().put(
                    "PATH",
                    "C:\\hadoop\\bin;"
                            + "C:\\Spark\\spark-3.5.6-bin-hadoop3-scala2.13\\bin;"
                            + processBuilder.environment().get("PATH")
            );
            processBuilder.directory(
                    new File(
                            System.getProperty("user.dir")
                    )
            );
            processBuilder.redirectErrorStream(true);
            Process process =
                    processBuilder.start();
            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(
                                         process.getInputStream()
                                 )
                         )) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info(
                            "[PySpark] {}",
                            line
                    );
                }
            }
            int exitCode =
                    process.waitFor();
            if (exitCode != 0) {
                log.error(
                        "PySpark splitter failed. Exit code: {}",
                        exitCode
                );
                throw new IngestionException(
                        "PySpark file splitting failed"
                );
            }
            log.info(
                    "PySpark file splitting completed successfully."
            );

            /**
            *  Collect all generated chunks dynamically instead of assuming fixed chunk count.
             */
            List<Path> chunkFiles;
            try (var files =
                         Files.list(splitOutputDirectory)) {
                chunkFiles =
                        files
                                .filter(Files::isRegularFile)
                                .filter(path ->
                                        path.getFileName()
                                                .toString()
                                                .startsWith("part-")
                                )
                                .filter(path ->
                                        path.getFileName()
                                                .toString()
                                                .endsWith("." + format)
                                )
                                .sorted(
                                        Comparator.comparing(
                                                path ->
                                                        path.getFileName()
                                                                .toString()
                                        )
                                )
                                .toList();
            }
            if (chunkFiles.isEmpty()) {
                throw new IngestionException(
                        "PySpark completed but no chunk files were generated"
                );
            }
            log.info(
                    "Number of chunk files generated: {}",
                    chunkFiles.size()
            );
            for (Path chunkFile : chunkFiles) {
                log.info(
                        "Generated chunk: {}",
                        chunkFile
                );
            }
            List<String> airflowChunkLocations =
                    new ArrayList<>();
            String airflowInputRoot =
                    "/opt/airflow/input/phase2_split/"
                            + executionId;
            for (Path chunkFile : chunkFiles) {
                String chunkFileName =
                        chunkFile.getFileName()
                                .toString();
                airflowChunkLocations.add(
                        airflowInputRoot
                                + "/"
                                + chunkFileName
                );
            }
            log.info(
                    "Airflow chunk locations: {}",
                    airflowChunkLocations
            );

            /**
            *  Pass the generated chunk locations to airflow for beam ingestion.
             */
            airflowClient.triggerDagWithChunks(
                    airflowDagId,
                    airflowChunkLocations,
                    airflowControlFileLocation,
                    executionId
            );
            return new IngestionResponse(
                    executionId,
                    "Large file split successfully into "
                            + chunkFiles.size()
                            + " chunks. Airflow DAG triggered."
            );
        } catch (IOException e) {
            log.error(
                    "Failed during ingestion processing",
                    e
            );
            throw new IngestionException(
                    "Unable to process uploaded file",
                    e
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(
                    "PySpark process was interrupted",
                    e
            );
            throw new IngestionException(
                    "PySpark process was interrupted",
                    e
            );
        }
    }
}
