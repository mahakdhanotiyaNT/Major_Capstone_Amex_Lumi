package com.amex.lumi.beam.validation;

import com.amex.lumi.beam.model.Employee;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeValidatorTest {

    private final EmployeeValidator validator =
            new EmployeeValidator();

    @Test
    void shouldReturnNoErrorsForValidEmployee() {

        Employee employee = createValidEmployee();

        List<String> errors = validator.validate(employee);

        assertTrue(errors.isEmpty());
    }

    @Test
    void shouldReturnErrorWhenEmployeeIsNull() {

        List<String> errors = validator.validate(null);

        assertEquals(
                List.of("Employee record is null"),
                errors
        );
    }

    @Test
    void shouldValidateEmployeeId() {

        Employee employee = createValidEmployee();
        employee.setEmployeeId("EMP01");

        List<String> errors = validator.validate(employee);

        assertTrue(
                errors.contains(
                        "employee_id must be exactly 7 characters"
                )
        );
    }

    @Test
    void shouldValidateFirstName() {

        Employee employee = createValidEmployee();
        employee.setFirstName("Jo");

        List<String> errors = validator.validate(employee);

        assertTrue(
                errors.contains(
                        "first_name must be between 3 and 15 characters"
                )
        );
    }

    @Test
    void shouldValidateLastNameLength() {

        Employee employee = createValidEmployee();
        employee.setLastName("ABCDEFGHIJKLMNOP");

        List<String> errors = validator.validate(employee);

        assertTrue(
                errors.contains(
                        "last_name must not exceed 15 characters"
                )
        );
    }

    @Test
    void shouldValidateEmailLength() {

        Employee employee = createValidEmployee();
        employee.setEmail("short");

        List<String> errors = validator.validate(employee);

        assertTrue(
                errors.contains(
                        "email must be between 13 and 30 characters"
                )
        );
    }

    @Test
    void shouldValidatePhoneNumber() {

        Employee employee = createValidEmployee();
        employee.setPhoneNumber("12345");

        List<String> errors = validator.validate(employee);

        assertTrue(
                errors.contains(
                        "phone_number must contain exactly 10 digits after removing country code and separators"
                )
        );
    }

    @Test
    void shouldAcceptIndianPhoneNumberWithCountryCode() {

        Employee employee = createValidEmployee();
        employee.setPhoneNumber("+91-98765-43210");

        List<String> errors = validator.validate(employee);

        assertFalse(
                errors.stream()
                        .anyMatch(error ->
                                error.startsWith("phone_number")
                        )
        );
    }

    @Test
    void shouldValidateHireDate() {

        Employee employee = createValidEmployee();
        employee.setHireDate("2024-99-99");

        List<String> errors = validator.validate(employee);

        assertTrue(
                errors.contains(
                        "hire_date must be a valid date in yyyy-MM-dd format"
                )
        );
    }

    @Test
    void shouldValidateSalary() {

        Employee employee = createValidEmployee();
        employee.setSalary("-100");

        List<String> errors = validator.validate(employee);

        assertTrue(
                errors.contains(
                        "salary must not be negative"
                )
        );
    }

    @Test
    void shouldRejectInvalidSalaryFormat() {

        Employee employee = createValidEmployee();
        employee.setSalary("abc");

        List<String> errors = validator.validate(employee);

        assertTrue(
                errors.contains(
                        "salary must be a valid number"
                )
        );
    }

    @Test
    void shouldValidateCurrency() {

        Employee employee = createValidEmployee();
        employee.setCurrency("IN");

        List<String> errors = validator.validate(employee);

        assertTrue(
                errors.contains(
                        "currency must be exactly 3 characters"
                )
        );
    }

    @Test
    void shouldValidateEmploymentStatus() {

        Employee employee = createValidEmployee();
        employee.setEmploymentStatus("A");

        List<String> errors = validator.validate(employee);

        assertTrue(
                errors.contains(
                        "employment_status must be between 3 and 13 characters"
                )
        );
    }

    @Test
    void shouldValidateManagerId() {

        Employee employee = createValidEmployee();
        employee.setManagerId("MGR01");

        List<String> errors = validator.validate(employee);

        assertTrue(
                errors.contains(
                        "Manager_id must be exactly 7 characters"
                )
        );
    }

    @Test
    void shouldValidateIsActive() {

        Employee employee = createValidEmployee();
        employee.setIsActive(null);

        List<String> errors = validator.validate(employee);

        assertTrue(
                errors.contains(
                        "is_active is required"
                )
        );
    }

    @Test
    void shouldValidateSkillsLength() {

        Employee employee = createValidEmployee();
        employee.setSkills(
                List.of("a".repeat(101))
        );

        List<String> errors = validator.validate(employee);

        assertTrue(
                errors.contains(
                        "skills must not exceed 100 characters"
                )
        );
    }

    @Test
    void shouldValidateMultipleFields() {

        Employee employee = createValidEmployee();

        employee.setEmployeeId("EMP");
        employee.setFirstName("A");
        employee.setSalary("-100");
        employee.setCurrency("IN");

        List<String> errors = validator.validate(employee);

        assertTrue(
                errors.contains(
                        "employee_id must be exactly 7 characters"
                )
        );

        assertTrue(
                errors.contains(
                        "first_name must be between 3 and 15 characters"
                )
        );

        assertTrue(
                errors.contains(
                        "salary must not be negative"
                )
        );

        assertTrue(
                errors.contains(
                        "currency must be exactly 3 characters"
                )
        );
    }

    private Employee createValidEmployee() {

        Employee employee = new Employee();

        employee.setEmployeeId("EMP0001");
        employee.setFirstName("Mahak");
        employee.setLastName("Dhanotiya");
        employee.setEmail("mahak@test.com");
        employee.setPhoneNumber("+91-9876543210");
        employee.setHireDate("2024-01-15");
        employee.setDepartment("Engineering");
        employee.setJobTitle("Developer");
        employee.setSalary("50000");
        employee.setCurrency("INR");
        employee.setEmploymentStatus("Active");
        employee.setManagerId("MGR0001");
        employee.setIsActive(true);
        employee.setSkills(
                List.of("Java", "Spring", "SQL")
        );

        return employee;
    }
}