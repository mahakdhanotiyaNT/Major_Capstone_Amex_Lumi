package com.amex.lumi.beam.parser;

import com.amex.lumi.beam.model.Address;
import com.amex.lumi.beam.model.Employee;
import com.amex.lumi.beam.model.EmergencyContact;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmployeeCsvParser {

    private static final String[] HEADERS = {
            "employee_id",
            "first_name",
            "last_name",
            "email",
            "phone_number",
            "hire_date",
            "department",
            "job_title",
            "salary",
            "currency",
            "employment_status",
            "manager_id",
            "is_active",
            "skills",
            "address_street",
            "address_city",
            "address_state",
            "address_postal_code",
            "address_country",
            "emergency_contact_name",
            "emergency_contact_relationship",
            "emergency_contact_phone",
            "emergency_contact_email"
    };

    public List<Employee> parse(String filePath) throws IOException {

        List<Employee> employees = new ArrayList<>();

        try (
                CSVParser csvParser = CSVParser.parse(
                        Path.of(filePath),
                        StandardCharsets.UTF_8,
                        CSVFormat.DEFAULT.builder()
                                .setHeader()
                                .setSkipHeaderRecord(true)
                                .setTrim(true)
                                .get()
                )
        ) {

            for (CSVRecord record : csvParser) {
                employees.add(parseRecord(record));
            }
        }

        return employees;
    }

    public Employee parseRecord(CSVRecord record) {

        Employee employee = new Employee();

        employee.setEmployeeId(record.get("employee_id"));
        employee.setFirstName(record.get("first_name"));
        employee.setLastName(record.get("last_name"));
        employee.setEmail(record.get("email"));
        employee.setPhoneNumber(record.get("phone_number"));
        employee.setHireDate(record.get("hire_date"));
        employee.setDepartment(record.get("department"));
        employee.setJobTitle(record.get("job_title"));
        employee.setSalary(record.get("salary"));
        employee.setCurrency(record.get("currency"));
        employee.setEmploymentStatus(record.get("employment_status"));
        employee.setManagerId(record.get("manager_id"));

        employee.setIsActive(
                Boolean.parseBoolean(record.get("is_active"))
        );

        String skillsValue = record.get("skills");

        employee.setSkills(
                Arrays.stream(skillsValue.split("\\|"))
                        .map(String::trim)
                        .toList()
        );

        Address address = new Address();

        address.setStreet(record.get("address_street"));
        address.setCity(record.get("address_city"));
        address.setState(record.get("address_state"));
        address.setPostalCode(record.get("address_postal_code"));
        address.setCountry(record.get("address_country"));

        employee.setAddress(address);

        EmergencyContact emergencyContact =
                new EmergencyContact();

        emergencyContact.setName(
                record.get("emergency_contact_name")
        );

        emergencyContact.setRelationship(
                record.get("emergency_contact_relationship")
        );

        emergencyContact.setPhone(
                record.get("emergency_contact_phone")
        );

        emergencyContact.setEmail(
                record.get("emergency_contact_email")
        );

        employee.setEmergencyContact(emergencyContact);

        return employee;
    }

    public Employee parseRecord(String csvLine) throws IOException {

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .setSkipHeaderRecord(false)
                .setTrim(true)
                .get();

        try (
                CSVParser parser = CSVParser.parse(
                        csvLine,
                        format
                )
        ) {
            CSVRecord record = parser.iterator().next();
            return parseRecord(record);
        }
    }
}