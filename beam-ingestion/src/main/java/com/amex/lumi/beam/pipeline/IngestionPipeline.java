package com.amex.lumi.beam.pipeline;

import com.amex.lumi.beam.model.Employee;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.values.TupleTagList;
import org.apache.beam.sdk.coders.StringUtf8Coder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IngestionPipeline {

    private static final Logger log =
            LoggerFactory.getLogger(IngestionPipeline.class);


    public static void main(String[] args){

        IngestionPipelineOptions options =
                PipelineOptionsFactory
                        .fromArgs(args)
                        .withValidation()
                        .as(IngestionPipelineOptions.class);


        log.info("Beam ingestion pipeline started");
        log.info("Processing file: {}", options.getFileLocation());
        log.info("Execution ID: {}", options.getExecutionId());

        Pipeline pipeline = Pipeline.create(options);

        // Create input file path
        PCollection<String> inputFile = pipeline
                .apply(
                        "Create Input File",
                        Create.of(options.getFileLocation())
                );

        // Parse input file based on file format
        PCollection<Employee> employees = inputFile
                .apply(
                        "Parse Employee",
                        ParDo.of(new ParseEmployeeDoFn())
                );

        PCollection<Employee> trimmedEmployees = employees
                .apply(
                        "Trim Whitespace",
                        ParDo.of(new TrimWhitespaceDoFn())
                );

        // Create tag for valid employees
        TupleTag<Employee> validEmployeesTag =
                new TupleTag<Employee>() {};

        // Validate employees
        PCollectionTuple validationResults = trimmedEmployees
                .apply(
                        "Validate Employee",
                        ParDo.of(new ValidateEmployeeDoFn())
                                .withOutputTags(
                                        validEmployeesTag,
                                        TupleTagList.of(
                                                ValidateEmployeeDoFn.INVALID_RECORDS
                                        )
                                )
                );

        // Get valid employees
        PCollection<Employee> validEmployees =
                validationResults.get(validEmployeesTag);

        // Get invalid records
        PCollection<String> invalidRecords =
                validationResults
                        .get(ValidateEmployeeDoFn.INVALID_RECORDS)
                        .setCoder(StringUtf8Coder.of());

        // Write invalid records to error file
        invalidRecords.apply(
                "Write Error Records",
                TextIO.write()
                        .to(options.getErrorFile())
                        .withNumShards(1)
                        .withoutSharding()
        );

        // Cleanse valid employees
        PCollection<Employee> cleansedEmployees =
                validEmployees.apply(
                        "Cleanse Employee",
                        ParDo.of(new CleansingDoFn())
                );

        // Get execution ID passed through pipeline options
        String executionId =
                options.getExecutionId();

        // 10. Get source file creation time
        String sourceCreationTime;

        try {
            sourceCreationTime =
                    java.nio.file.Files.readAttributes(
                            java.nio.file.Paths.get(
                                    options.getFileLocation()
                            ),
                            java.nio.file.attribute.BasicFileAttributes.class
                    ).creationTime().toString();

        } catch (java.io.IOException e) {

            throw new RuntimeException(
                    "Unable to read source file creation time",
                    e
            );
        }

        //  Encrypt sensitive fields
        String encryptionKey = System.getenv("ENCRYPTION_KEY");

        if (encryptionKey == null || encryptionKey.isBlank()) {
            throw new IllegalStateException(
                   "Required environment variable is missing: ENCRYPTION_KEY");
        }

        PCollection<Employee> encryptedEmployees =
                cleansedEmployees.apply(
                        "Encrypt Sensitive Fields",
                        ParDo.of(
                                new EncryptionDoFn(encryptionKey)
                        )
                );

        // Add metadata
        PCollection<Employee> metadataEmployees =
                encryptedEmployees.apply(
                        "Add Metadata",
                        ParDo.of(
                                new MetadataDoFn(
                                        executionId,
                                        sourceCreationTime
                                )
                        )
                );

        // Write employees to PostgreSQL
        metadataEmployees.apply(
                "Write Employees To PostgreSQL",
                ParDo.of(new WriteToPostgresDoFn())
        );


        pipeline.run().waitUntilFinish();

        log.info("Beam ingestion pipeline completed successfully");
    }
}