package com.amex.lumi.beam.parser;

import com.amex.lumi.beam.model.Employee;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeJsonParserTest {

    private final EmployeeJsonParser parser =
            new EmployeeJsonParser();

    @Test
    void shouldParseJsonArray() throws Exception {

        String json = """
                [
                  {
                    "employee_id": "EMP001",
                    "first_name": "John",
                    "last_name": "Doe",
                    "email": "john@test.com",
                    "phone_number": "9876543210",
                    "hire_date": "2024-01-01",
                    "department": "Engineering",
                    "job_title": "Developer",
                    "salary": "50000",
                    "currency": "INR",
                    "employment_status": "Active",
                    "manager_id": "MGR001",
                    "is_active": true,
                    "skills": ["Java", "Spring"],
                    "address": {
                      "street": "Main Street",
                      "city": "Mumbai",
                      "state": "Maharashtra",
                      "postal_code": "400001",
                      "country": "India"
                    },
                    "emergency_contact": {
                      "name": "Jane Doe",
                      "relationship": "Sister",
                      "phone": "9876543211",
                      "email": "jane@test.com"
                    }
                  },
                  {
                    "employee_id": "EMP002",
                    "first_name": "Jane",
                    "last_name": "Smith",
                    "email": "jane@test.com",
                    "phone_number": "9876543212",
                    "hire_date": "2023-05-10",
                    "department": "Finance",
                    "job_title": "Analyst",
                    "salary": "60000",
                    "currency": "INR",
                    "employment_status": "Active",
                    "manager_id": "MGR002",
                    "is_active": false,
                    "skills": ["SQL", "Excel"]
                  }
                ]
                """;

        Path file = Files.createTempFile(
                "employee-test",
                ".json"
        );

        Files.writeString(file, json);

        try {
            List<Employee> employees =
                    parser.parse(file.toString());

            assertEquals(2, employees.size());

            assertEquals(
                    "EMP001",
                    employees.get(0).getEmployeeId()
            );

            assertEquals(
                    "John",
                    employees.get(0).getFirstName()
            );

            assertEquals(
                    "EMP002",
                    employees.get(1).getEmployeeId()
            );

            assertFalse(
                    employees.get(1).getIsActive()
            );
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void shouldParseSingleJsonObject() throws Exception {

        String json = """
                {
                  "employee_id": "EMP001",
                  "first_name": "John",
                  "last_name": "Doe",
                  "email": "john@test.com",
                  "phone_number": "9876543210",
                  "hire_date": "2024-01-01",
                  "department": "Engineering",
                  "job_title": "Developer",
                  "salary": "50000",
                  "currency": "INR",
                  "employment_status": "Active",
                  "manager_id": "MGR001",
                  "is_active": true,
                  "skills": ["Java", "Spring"]
                }
                """;

        Path file = Files.createTempFile(
                "employee-test",
                ".json"
        );

        Files.writeString(file, json);

        try {
            List<Employee> employees =
                    parser.parse(file.toString());

            assertEquals(1, employees.size());

            Employee employee = employees.get(0);

            assertEquals("EMP001", employee.getEmployeeId());
            assertEquals("John", employee.getFirstName());
            assertEquals("Doe", employee.getLastName());
            assertEquals("john@test.com", employee.getEmail());
            assertTrue(employee.getIsActive());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void shouldMapSnakeCaseJsonFields() throws Exception {

        String json = """
                {
                  "employee_id": "EMP001",
                  "first_name": "John",
                  "last_name": "Doe",
                  "phone_number": "9876543210",
                  "job_title": "Developer",
                  "employment_status": "Active",
                  "manager_id": "MGR001",
                  "is_active": true
                }
                """;

        Path file = Files.createTempFile(
                "employee-test",
                ".json"
        );

        Files.writeString(file, json);

        try {
            Employee employee =
                    parser.parse(file.toString()).get(0);

            assertEquals("EMP001", employee.getEmployeeId());
            assertEquals("John", employee.getFirstName());
            assertEquals("Doe", employee.getLastName());
            assertEquals("9876543210", employee.getPhoneNumber());
            assertEquals("Developer", employee.getJobTitle());
            assertEquals("Active", employee.getEmploymentStatus());
            assertEquals("MGR001", employee.getManagerId());
            assertTrue(employee.getIsActive());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void shouldThrowExceptionForInvalidJsonStructure()
            throws Exception {

        String json = "123";

        Path file = Files.createTempFile(
                "employee-test",
                ".json"
        );

        Files.writeString(file, json);

        try {
            IOException exception =
                    assertThrows(
                            IOException.class,
                            () -> parser.parse(file.toString())
                    );

            assertEquals(
                    "Invalid JSON format. Expected JSON object or array.",
                    exception.getMessage()
            );
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void shouldThrowExceptionForMissingFile() {

        assertThrows(
                IOException.class,
                () -> parser.parse(
                        "non-existing-employee-file.json"
                )
        );
    }
}