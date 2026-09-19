package com.amex.lumi.beam.pipeline;

import com.amex.lumi.beam.model.Employee;
import com.amex.lumi.beam.parser.EmployeeCsvParser;
import com.amex.lumi.beam.parser.EmployeeJsonParser;
import org.apache.beam.sdk.transforms.DoFn;

import java.io.IOException;
import java.util.List;

public class ParseEmployeeDoFn extends DoFn<String, Employee> {

    private transient EmployeeCsvParser csvParser;
    private transient EmployeeJsonParser jsonParser;

    @Setup
    public void setup() {
        csvParser = new EmployeeCsvParser();
        jsonParser = new EmployeeJsonParser();
    }

    @ProcessElement
    public void processElement(ProcessContext context) throws IOException {

        String filePath = context.element();

        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        String lowerCasePath = filePath.toLowerCase();

        if (lowerCasePath.endsWith(".csv")) {

            List<Employee> employees = csvParser.parse(filePath);

            for (Employee employee : employees) {
                context.output(employee);
            }

        } else if (lowerCasePath.endsWith(".json")) {

            List<Employee> employees = jsonParser.parse(filePath);

            for (Employee employee : employees) {
                context.output(employee);
            }

        } else {
            throw new IllegalArgumentException(
                    "Unsupported file format. Only CSV and JSON are supported."
            );
        }
    }
}