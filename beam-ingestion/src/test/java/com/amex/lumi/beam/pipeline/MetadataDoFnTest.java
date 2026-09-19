package com.amex.lumi.beam.pipeline;

import com.amex.lumi.beam.model.Employee;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MetadataDoFnTest {

    @Test
    void shouldAddMetadataToEmployee() {

        Employee employee = createEmployee();

        String executionId = "EXEC-001";
        String sourceCreationTime = "2026-09-15T10:00:00Z";

        Pipeline pipeline = Pipeline.create();

        pipeline
                .apply(
                        "Create Employee",
                        Create.of(employee)
                )
                .apply(
                        "Add Metadata",
                        ParDo.of(
                                new MetadataDoFn(
                                        executionId,
                                        sourceCreationTime
                                )
                        )
                )
                .apply(
                        "Verify Metadata",
                        ParDo.of(
                                new MetadataAssertionDoFn(
                                        executionId,
                                        sourceCreationTime
                                )
                        )
                );

        pipeline.run().waitUntilFinish();
    }

    private static Employee createEmployee() {

        Employee employee = new Employee();

        employee.setEmployeeId("EMP0001");
        employee.setFirstName("Mahak");
        employee.setLastName("Dhanotiya");
        employee.setEmail("mahak@test.com");
        employee.setPhoneNumber("9876543210");
        employee.setHireDate("2024-01-15");
        employee.setDepartment("Engineering");
        employee.setJobTitle("Developer");
        employee.setSalary("50000");
        employee.setCurrency("INR");
        employee.setEmploymentStatus("Active");
        employee.setManagerId("MGR0001");
        employee.setIsActive(true);
        employee.setSkills(List.of("Java", "Spring"));

        return employee;
    }

    private static class MetadataAssertionDoFn
            extends DoFn<Employee, Void> {

        private final String expectedExecutionId;
        private final String expectedSourceCreationTime;

        MetadataAssertionDoFn(
                String expectedExecutionId,
                String expectedSourceCreationTime) {

            this.expectedExecutionId = expectedExecutionId;
            this.expectedSourceCreationTime =
                    expectedSourceCreationTime;
        }

        @ProcessElement
        public void processElement(ProcessContext context) {

            Employee employee = context.element();

            assertEquals(
                    expectedExecutionId,
                    employee.getExecutionId()
            );

            assertEquals(
                    expectedSourceCreationTime,
                    employee.getSourceCreationTime()
            );

            assertNotNull(
                    employee.getIngestionTimestamp()
            );

            Instant.parse(
                    employee.getIngestionTimestamp()
            );

            assertEquals(
                    "EMP0001",
                    employee.getEmployeeId()
            );
        }
    }
}