package com.amex.lumi.beam.parser;

import com.amex.lumi.beam.model.Employee;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeJsonParser {

    private final ObjectMapper objectMapper;

    public EmployeeJsonParser() {
        objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(
                PropertyNamingStrategies.SNAKE_CASE
        );
    }

    public List<Employee> parse(String filePath) throws IOException {

        JsonNode root =
                objectMapper.readTree(
                        new java.io.File(filePath)
                );

        List<Employee> employees =
                new ArrayList<>();

        if (root.isArray()) {

            for (JsonNode node : root) {

                Employee employee =
                        objectMapper.treeToValue(
                                node,
                                Employee.class
                        );

                employees.add(employee);
            }

        } else if (root.isObject()) {

            Employee employee =
                    objectMapper.treeToValue(
                            root,
                            Employee.class
                    );

            employees.add(employee);

        } else {

            throw new IOException(
                    "Invalid JSON format. Expected JSON object or array."
            );
        }

        return employees;
    }
}