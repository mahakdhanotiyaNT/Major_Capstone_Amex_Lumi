package com.amex.lumi.beam.cleansing;

import com.amex.lumi.beam.model.Address;
import com.amex.lumi.beam.model.Employee;
import com.amex.lumi.beam.model.EmergencyContact;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeCleansingTest {

    private final EmployeeCleansing cleansing =
            new EmployeeCleansing();

    @Test
    void shouldReplaceNullStringFieldsWithWhitespace() {

        Employee employee = new Employee();

        employee.setFirstName(null);
        employee.setLastName(null);
        employee.setEmail(null);
        employee.setDepartment(null);
        employee.setJobTitle(null);
        employee.setSalary(null);
        employee.setCurrency(null);

        Employee result = cleansing.cleanse(employee);

        assertEquals(" ", result.getFirstName());
        assertEquals(" ", result.getLastName());
        assertEquals(" ", result.getEmail());
        assertEquals(" ", result.getDepartment());
        assertEquals(" ", result.getJobTitle());
        assertEquals(" ", result.getSalary());
        assertEquals(" ", result.getCurrency());
    }

    @Test
    void shouldReplaceBlankStringFieldsWithWhitespace() {

        Employee employee = new Employee();

        employee.setFirstName("");
        employee.setLastName("   ");
        employee.setDepartment("");
        employee.setJobTitle("   ");

        Employee result = cleansing.cleanse(employee);

        assertEquals(" ", result.getFirstName());
        assertEquals(" ", result.getLastName());
        assertEquals(" ", result.getDepartment());
        assertEquals(" ", result.getJobTitle());
    }

    @Test
    void shouldKeepExistingValuesUnchanged() {

        Employee employee = new Employee();

        employee.setFirstName("Arjun");
        employee.setLastName("Sharma");
        employee.setDepartment("Engineering");
        employee.setSalary("950000");

        Employee result = cleansing.cleanse(employee);

        assertEquals("Arjun", result.getFirstName());
        assertEquals("Sharma", result.getLastName());
        assertEquals("Engineering", result.getDepartment());
        assertEquals("950000", result.getSalary());
    }

    @Test
    void shouldCleanseAddressFields() {

        Employee employee = new Employee();

        Address address = new Address();

        address.setStreet(null);
        address.setCity("");
        address.setState("Karnataka");
        address.setPostalCode(null);
        address.setCountry("India");

        employee.setAddress(address);

        Employee result = cleansing.cleanse(employee);

        assertEquals(" ", result.getAddress().getStreet());
        assertEquals(" ", result.getAddress().getCity());
        assertEquals("Karnataka", result.getAddress().getState());
        assertEquals(" ", result.getAddress().getPostalCode());
        assertEquals("India", result.getAddress().getCountry());
    }

    @Test
    void shouldCleanseEmergencyContactFields() {

        Employee employee = new Employee();

        EmergencyContact contact = new EmergencyContact();

        contact.setName(null);
        contact.setRelationship("");
        contact.setPhone("9876543210");
        contact.setEmail(null);

        employee.setEmergencyContact(contact);

        Employee result = cleansing.cleanse(employee);

        assertEquals(" ", result.getEmergencyContact().getName());
        assertEquals(" ", result.getEmergencyContact().getRelationship());
        assertEquals("9876543210",
                result.getEmergencyContact().getPhone());
        assertEquals(" ", result.getEmergencyContact().getEmail());
    }

    @Test
    void shouldHandleNullAddress() {

        Employee employee = new Employee();
        employee.setAddress(null);

        Employee result = cleansing.cleanse(employee);

        assertNull(result.getAddress());
    }

    @Test
    void shouldHandleNullEmergencyContact() {

        Employee employee = new Employee();
        employee.setEmergencyContact(null);

        Employee result = cleansing.cleanse(employee);

        assertNull(result.getEmergencyContact());
    }

    @Test
    void shouldHandleNullEmployee() {

        Employee result = cleansing.cleanse(null);

        assertNull(result);
    }
}