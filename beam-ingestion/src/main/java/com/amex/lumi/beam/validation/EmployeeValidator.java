package com.amex.lumi.beam.validation;

import com.amex.lumi.beam.model.Employee;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeValidator {

    public List<String> validate(Employee employee) {

        List<String> errors = new ArrayList<>();

        if (employee == null) {
            errors.add("Employee record is null");
            return errors;
        }

        validateEmployeeId(employee, errors);
        validateFirstName(employee, errors);
        validateLastName(employee, errors);
        validateEmail(employee, errors);
        validatePhoneNumber(employee, errors);
        validateHireDate(employee, errors);
        validateDepartment(employee, errors);
        validateJobTitle(employee, errors);
        validateSalary(employee, errors);
        validateCurrency(employee, errors);
        validateEmploymentStatus(employee, errors);
        validateManagerId(employee, errors);
        validateIsActive(employee, errors);
        validateSkills(employee, errors);

        return errors;
    }

    private void validateEmployeeId(Employee employee, List<String> errors) {

        String value = employee.getEmployeeId();

        if (value == null || value.isBlank()) {
            errors.add("employee_id is required");
            return;
        }

        if (value.length() != 7) {
            errors.add("employee_id must be exactly 7 characters");
        }
    }

    private void validateFirstName(Employee employee, List<String> errors) {

        String value = employee.getFirstName();

        if (value == null || value.isBlank()) {
            errors.add("first_name is required");
            return;
        }

        if (value.length() < 3 || value.length() > 15) {
            errors.add("first_name must be between 3 and 15 characters");
        }
    }

    private void validateLastName(Employee employee, List<String> errors) {

        String value = employee.getLastName();

        if (value != null && value.length() > 15) {
            errors.add("last_name must not exceed 15 characters");
        }
    }

    private void validateEmail(Employee employee, List<String> errors) {

        String value = employee.getEmail();

        if (value == null || value.isBlank()) {
            errors.add("email is required");
            return;
        }

        if (value.length() < 13 || value.length() > 30) {
            errors.add("email must be between 13 and 30 characters");
        }

         if (!value.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            errors.add(
                    "emergency_contact.email has invalid format"
            );
        }
    }

    private void validatePhoneNumber(Employee employee, List<String> errors) {

        String value = employee.getPhoneNumber();

        if (value == null || value.isBlank()) {
            errors.add("phone_number is required");
            return;
        }


        String normalizedPhone = value;

        if (normalizedPhone.startsWith("+91")) {
            normalizedPhone = normalizedPhone.substring(3);
        }

        // Remove spaces, hyphens and other non-digit characters.
        normalizedPhone = normalizedPhone.replaceAll("\\D", "");

        if (normalizedPhone.length() != 10) {
            errors.add(
                    "phone_number must contain exactly 10 digits after removing country code and separators"
            );
        }
    }

    private void validateHireDate(Employee employee, List<String> errors) {

        String value = employee.getHireDate();

        if (value == null || value.length() != 10) {
            errors.add("hire_date must be exactly 10 characters");
            return;
        }


        try {
            LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            errors.add("hire_date must be a valid date in yyyy-MM-dd format");
        }
    }

    private void validateDepartment(Employee employee, List<String> errors) {

        String value = employee.getDepartment();

        if (value != null && value.length() > 20) {
            errors.add("department must not exceed 20 characters");
        }
    }

    private void validateJobTitle(Employee employee, List<String> errors) {

        String value = employee.getJobTitle();

        if (value != null && value.length() > 30) {
            errors.add("job_title must not exceed 30 characters");
        }
    }

    private void validateSalary(Employee employee, List<String> errors) {

        String value = employee.getSalary();

        if (value == null || value.isBlank()) {
            errors.add("salary is required");
            return;
        }

        try {
            double salary = Double.parseDouble(value);

            if (salary < 0) {
                errors.add("salary must not be negative");
            }

        } catch (NumberFormatException exception) {
            errors.add("salary must be a valid number");
        }
    }

    private void validateCurrency(Employee employee, List<String> errors) {

        String value = employee.getCurrency();

        if (value == null || value.length() != 3) {
            errors.add("currency must be exactly 3 characters");
        }
    }

    private void validateEmploymentStatus(
            Employee employee,
            List<String> errors) {

        String value = employee.getEmploymentStatus();

        if (value == null || value.isBlank()) {
            errors.add("employment_status is required");
            return;
        }

        if (value.length() < 3 || value.length() > 13) {
            errors.add(
                    "employment_status must be between 3 and 13 characters"
            );
        }
    }

    private void validateManagerId(Employee employee, List<String> errors) {

        String value = employee.getManagerId();

        if (value == null || value.isBlank()) {
            errors.add("Manager_id is required");
            return;
        }

        if (value.length() != 7) {
            errors.add("Manager_id must be exactly 7 characters");
        }
    }

    private void validateIsActive(Employee employee, List<String> errors) {


        if (employee.getIsActive() == null) {
            errors.add("is_active is required");
        }
    }

    private void validateSkills(Employee employee, List<String> errors) {

        List<String> skills = employee.getSkills();

        if (skills == null) {
            return;
        }

        String skillsValue = String.join(
                "|",
                skills.stream()
                        .map(skill -> skill == null ? "" : skill)
                        .toList()
        );

        if (skillsValue.length() > 100) {
            errors.add("skills must not exceed 100 characters");
        }
    }
}