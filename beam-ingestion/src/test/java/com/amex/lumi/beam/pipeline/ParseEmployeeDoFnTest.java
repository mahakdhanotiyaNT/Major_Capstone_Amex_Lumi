package com.amex.lumi.beam.pipeline;

import com.amex.lumi.beam.model.Employee;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.ParDo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParseEmployeeDoFnTest {

    @Test
    void shouldParseCsvFile() throws IOException {

        Path file = Files.createTempFile("employees", ".csv");

        Files.writeString(
                file,
                """
                employee_id,first_name,last_name,email,phone_number,hire_date,department,job_title,salary,currency,employment_status,manager_id,is_active,skills,address_street,address_city,address_state,address_postal_code,address_country,emergency_contact_name,emergency_contact_relationship,emergency_contact_phone,emergency_contact_email
                EMP0001,Mahak,Dhanotiya,mahak@test.com,9876543210,2024-01-15,Engineering,Developer,50000,INR,Active,MGR0001,true,Java|Spring,Street 1,Raigarh,CG,496001,India,Test Contact,Sister,9876543211,contact@test.com
                """
        );

        Pipeline pipeline = Pipeline.create();

        var employees = pipeline
                .apply(
                        "Create CSV File Path",
                        Create.of(file.toString())
                )
                .apply(
                        "Parse CSV",
                        ParDo.of(new ParseEmployeeDoFn())
                );

        PAssert.that(employees).satisfies(results -> {

            int count = 0;

            for (Employee employee : results) {
                assertEquals("EMP0001", employee.getEmployeeId());
                assertEquals("Mahak", employee.getFirstName());
                assertEquals("Dhanotiya", employee.getLastName());
                count++;
            }

            assertEquals(1, count);

            return null;
        });

        pipeline.run().waitUntilFinish();

        Files.deleteIfExists(file);
    }

    @Test
    void shouldParseJsonFile() throws IOException {

        Path file = Files.createTempFile("employees", ".json");

        Files.writeString(
                file,
                """
                [
                  {
                    "employee_id": "EMP0001",
                    "first_name": "Mahak",
                    "last_name": "Dhanotiya",
                    "email": "mahak@test.com",
                    "phone_number": "9876543210",
                    "hire_date": "2024-01-15",
                    "department": "Engineering",
                    "job_title": "Developer",
                    "salary": "50000",
                    "currency": "INR",
                    "employment_status": "Active",
                    "manager_id": "MGR0001",
                    "is_active": true,
                    "skills": ["Java", "Spring"]
                  }
                ]
                """
        );

        Pipeline pipeline = Pipeline.create();

        var employees = pipeline
                .apply(
                        "Create JSON File Path",
                        Create.of(file.toString())
                )
                .apply(
                        "Parse JSON",
                        ParDo.of(new ParseEmployeeDoFn())
                );

        PAssert.that(employees).satisfies(results -> {

            int count = 0;

            for (Employee employee : results) {
                assertEquals("EMP0001", employee.getEmployeeId());
                assertEquals("Mahak", employee.getFirstName());
                assertEquals("Dhanotiya", employee.getLastName());
                count++;
            }

            assertEquals(1, count);

            return null;
        });

        pipeline.run().waitUntilFinish();

        Files.deleteIfExists(file);
    }
}