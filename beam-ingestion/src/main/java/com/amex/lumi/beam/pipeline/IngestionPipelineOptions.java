package com.amex.lumi.beam.pipeline;

import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;

public interface IngestionPipelineOptions
        extends PipelineOptions {

    @Description("Location of the input data file")
    String getFileLocation();

    void setFileLocation(String fileLocation);

    @Description("Unique ID for the ingestion run")
    String getExecutionId();

    void setExecutionId(String executionId);

    @Description("Location of the error records file")
    String getErrorFile();

    void setErrorFile(String errorFile);
}
