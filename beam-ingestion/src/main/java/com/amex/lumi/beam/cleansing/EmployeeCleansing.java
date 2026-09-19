package com.amex.lumi.beam.cleansing;

import com.amex.lumi.beam.model.Address;
import com.amex.lumi.beam.model.Employee;
import com.amex.lumi.beam.model.EmergencyContact;

public class EmployeeCleansing {

    public Employee cleanse(Employee employee) {

        if (employee == null) {
            return null;
        }

        cleanseEmployeeFields(employee);
        cleanseAddress(employee);
        cleanseEmergencyContact(employee);

        return employee;
    }

    private void cleanseEmployeeFields(Employee employee) {

        employee.setEmployeeId(toWhitespace(employee.getEmployeeId()));
        employee.setFirstName(toWhitespace(employee.getFirstName()));
        employee.setLastName(toWhitespace(employee.getLastName()));
        employee.setEmail(toWhitespace(employee.getEmail()));
        employee.setPhoneNumber(toWhitespace(employee.getPhoneNumber()));
        employee.setHireDate(toWhitespace(employee.getHireDate()));
        employee.setDepartment(toWhitespace(employee.getDepartment()));
        employee.setJobTitle(toWhitespace(employee.getJobTitle()));
        employee.setSalary(toWhitespace(employee.getSalary()));
        employee.setCurrency(toWhitespace(employee.getCurrency()));
        employee.setEmploymentStatus(
                toWhitespace(employee.getEmploymentStatus())
        );
        employee.setManagerId(toWhitespace(employee.getManagerId()));
    }

    private void cleanseAddress(Employee employee) {

        Address address = employee.getAddress();

        if (address == null) {
            return;
        }

        address.setStreet(toWhitespace(address.getStreet()));
        address.setCity(toWhitespace(address.getCity()));
        address.setState(toWhitespace(address.getState()));
        address.setPostalCode(toWhitespace(address.getPostalCode()));
        address.setCountry(toWhitespace(address.getCountry()));
    }

    private void cleanseEmergencyContact(Employee employee) {

        EmergencyContact emergencyContact =
                employee.getEmergencyContact();

        if (emergencyContact == null) {
            return;
        }

        emergencyContact.setName(
                toWhitespace(emergencyContact.getName())
        );

        emergencyContact.setRelationship(
                toWhitespace(emergencyContact.getRelationship())
        );

        emergencyContact.setPhone(
                toWhitespace(emergencyContact.getPhone())
        );

        emergencyContact.setEmail(
                toWhitespace(emergencyContact.getEmail())
        );
    }

    private String toWhitespace(String value) {

        if (value == null || value.isBlank()) {
            return " ";
        }

        return value;
    }
}
