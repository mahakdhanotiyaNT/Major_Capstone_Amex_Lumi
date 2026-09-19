package com.amex.lumi.beam.pipeline;

import com.amex.lumi.beam.model.Employee;
import org.apache.beam.sdk.transforms.DoFn;

public class TrimWhitespaceDoFn extends DoFn<Employee, Employee> {

    @ProcessElement
    public void processElement(ProcessContext context) {

        Employee employee = context.element();

        Employee trimmedEmployee = new Employee();

        trimmedEmployee.setEmployeeId(trim(employee.getEmployeeId()));
        trimmedEmployee.setFirstName(trim(employee.getFirstName()));
        trimmedEmployee.setLastName(trim(employee.getLastName()));
        trimmedEmployee.setEmail(trim(employee.getEmail()));
        trimmedEmployee.setPhoneNumber(trim(employee.getPhoneNumber()));
        trimmedEmployee.setHireDate(trim(employee.getHireDate()));
        trimmedEmployee.setDepartment(trim(employee.getDepartment()));
        trimmedEmployee.setJobTitle(trim(employee.getJobTitle()));
        trimmedEmployee.setSalary(trim(employee.getSalary()));
        trimmedEmployee.setCurrency(trim(employee.getCurrency()));
        trimmedEmployee.setEmploymentStatus(trim(employee.getEmploymentStatus()));
        trimmedEmployee.setManagerId(trim(employee.getManagerId()));

        // Keep other fields unchanged
        trimmedEmployee.setIsActive(employee.getIsActive());
        trimmedEmployee.setSkills(employee.getSkills());
        trimmedEmployee.setAddress(employee.getAddress());
        trimmedEmployee.setEmergencyContact(employee.getEmergencyContact());


        context.output(trimmedEmployee);
    }

    private String trim(String value) {
        return value == null ? null : value.strip();
    }
}