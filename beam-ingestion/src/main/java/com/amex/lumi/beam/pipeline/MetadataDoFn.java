package com.amex.lumi.beam.pipeline;

import com.amex.lumi.beam.model.Employee;
import org.apache.beam.sdk.transforms.DoFn;

import java.time.Instant;

public class MetadataDoFn
        extends DoFn<Employee, Employee> {

    private final String executionId;
    private final String sourceCreationTime;

    public MetadataDoFn(
            String executionId,
            String sourceCreationTime) {

        this.executionId = executionId;
        this.sourceCreationTime = sourceCreationTime;
    }

    @ProcessElement
    public void processElement(ProcessContext context) {

        Employee oldEmployee = context.element();

        Employee employee = new Employee();

        employee.setEmployeeId(oldEmployee.getEmployeeId());
        employee.setFirstName(oldEmployee.getFirstName());
        employee.setLastName(oldEmployee.getLastName());
        employee.setEmail(oldEmployee.getEmail());
        employee.setPhoneNumber(oldEmployee.getPhoneNumber());
        employee.setHireDate(oldEmployee.getHireDate());
        employee.setDepartment(oldEmployee.getDepartment());
        employee.setJobTitle(oldEmployee.getJobTitle());
        employee.setSalary(oldEmployee.getSalary());
        employee.setCurrency(oldEmployee.getCurrency());
        employee.setEmploymentStatus(oldEmployee.getEmploymentStatus());
        employee.setManagerId(oldEmployee.getManagerId());
        employee.setIsActive(oldEmployee.getIsActive());
        employee.setSkills(oldEmployee.getSkills());
        employee.setAddress(oldEmployee.getAddress());
        employee.setEmergencyContact(oldEmployee.getEmergencyContact());

        // Add metadata to the NEW object
        employee.setExecutionId(executionId);

        employee.setIngestionTimestamp(
                Instant.now().toString()
        );

        employee.setSourceCreationTime(
                sourceCreationTime
        );

        context.output(employee);
    }
}