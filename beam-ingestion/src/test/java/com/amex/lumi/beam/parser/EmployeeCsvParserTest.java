package com.amex.lumi.beam.parser;

import com.amex.lumi.beam.model.Employee;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmployeeCsvParserTest {

    @Test
    void shouldParseCsvFileSuccessfully() throws IOException {

        Path file = Files.createTempFile(
                "employees",
                ".csv"
        );

        Files.writeString(
                file,
                """
                employee_id,first_name,last_name,email,phone_number,hire_date,department,job_title,salary,currency,employment_status,manager_id,is_active,skills,address_street,address_city,address_state,address_postal_code,address_country,emergency_contact_name,emergency_contact_relationship,emergency_contact_phone,emergency_contact_email
                EMP0001,Mahak,Dhanotiya,mahak@test.com,9876543210,2024-01-15,Engineering,Developer,50000,INR,Active,MGR0001,true,Java|Spring,Street 1,Indore,MP,452001,India,Riya,Sister,9876543211,riya@test.com
                """
        );

        EmployeeCsvParser parser =
                new EmployeeCsvParser();

        List<Employee> employees =
                parser.parse(file.toString());

        assertEquals(1, employees.size());
        assertEquals(
                "EMP0001",
                employees.get(0).getEmployeeId()
        );
        assertEquals(
                "Mahak",
                employees.get(0).getFirstName()
        );
        assertEquals(
                "Dhanotiya",
                employees.get(0).getLastName()
        );
        assertEquals(
                "mahak@test.com",
                employees.get(0).getEmail()
        );

        Files.deleteIfExists(file);
    }

    @Test
    void shouldParseCsvLineSuccessfully() throws IOException {

        String csvLine =
                "EMP0001,Mahak,Dhanotiya,mahak@test.com,"
                        + "9876543210,2024-01-15,Engineering,Developer,"
                        + "50000,INR,Active,MGR0001,true,Java|Spring,"
                        + "Street 1,Indore,MP,452001,India,Riya,Sister,"
                        + "9876543211,riya@test.com";

        EmployeeCsvParser parser =
                new EmployeeCsvParser();

        Employee employee =
                parser.parseRecord(csvLine);

        assertEquals(
                "EMP0001",
                employee.getEmployeeId()
        );
        assertEquals(
                "Mahak",
                employee.getFirstName()
        );
        assertEquals(
                "Dhanotiya",
                employee.getLastName()
        );
        assertEquals(
                "mahak@test.com",
                employee.getEmail()
        );
    }

    @Test
    void shouldParseMultipleSkills() throws IOException {

        String csvLine =
                "EMP0001,Mahak,Dhanotiya,mahak@test.com,"
                        + "9876543210,2024-01-15,Engineering,Developer,"
                        + "50000,INR,Active,MGR0001,true,Java|Spring|SQL,"
                        + "Street 1,Indore,MP,452001,India,Riya,Sister,"
                        + "9876543211,riya@test.com";

        EmployeeCsvParser parser =
                new EmployeeCsvParser();

        Employee employee =
                parser.parseRecord(csvLine);

        assertEquals(
                3,
                employee.getSkills().size()
        );
        assertEquals(
                "Java",
                employee.getSkills().get(0)
        );
        assertEquals(
                "Spring",
                employee.getSkills().get(1)
        );
        assertEquals(
                "SQL",
                employee.getSkills().get(2)
        );
    }

    @Test
    void shouldParseBooleanValue() throws IOException {

        String csvLine =
                "EMP0001,Mahak,Dhanotiya,mahak@test.com,"
                        + "9876543210,2024-01-15,Engineering,Developer,"
                        + "50000,INR,Active,MGR0001,true,Java,"
                        + "Street 1,Indore,MP,452001,India,Riya,Sister,"
                        + "9876543211,riya@test.com";

        EmployeeCsvParser parser =
                new EmployeeCsvParser();

        Employee employee =
                parser.parseRecord(csvLine);

        assertEquals(
                true,
                employee.getIsActive()
        );
    }

    @Test
    void shouldParseAddressAndEmergencyContact() throws IOException {

        String csvLine =
                "EMP0001,Mahak,Dhanotiya,mahak@test.com,"
                        + "9876543210,2024-01-15,Engineering,Developer,"
                        + "50000,INR,Active,MGR0001,true,Java,"
                        + "Street 1,Indore,MP,452001,India,Riya,Sister,"
                        + "9876543211,riya@test.com";

        EmployeeCsvParser parser =
                new EmployeeCsvParser();

        Employee employee =
                parser.parseRecord(csvLine);

        assertEquals(
                "Street 1",
                employee.getAddress().getStreet()
        );
        assertEquals(
                "Indore",
                employee.getAddress().getCity()
        );
        assertEquals(
                "Riya",
                employee.getEmergencyContact().getName()
        );
        assertEquals(
                "9876543211",
                employee.getEmergencyContact().getPhone()
        );
    }
}