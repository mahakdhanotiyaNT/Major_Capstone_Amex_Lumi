package com.amex.lumi.ingestion.service;

import com.amex.lumi.ingestion.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import com.amex.lumi.ingestion.exception.EmployeeNotFoundException;

import java.util.List;
import java.util.Map;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DecryptionService decryptionService;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            DecryptionService decryptionService) {
        this.employeeRepository = employeeRepository;
        this.decryptionService = decryptionService;
    }

    public List<Map<String, Object>> getAllEmployees() {
        List<Map<String, Object>> employees =
                employeeRepository.findAll();

        employees.forEach(this::decryptEmployee);

        return employees;
    }

    public Map<String, Object> getEmployeeById(String employeeId) {
        Map<String, Object> employee =
                employeeRepository.findByEmployeeId(employeeId);

        if (employee == null) {
            throw new EmployeeNotFoundException(
                    "Employee not found with ID: " + employeeId
            );
        }

        decryptEmployee(employee);

        return employee;
    }

    private void decryptEmployee(Map<String, Object> employee) {
        employee.put(
                "phone_number",
                decryptionService.decrypt(
                        (String) employee.get("phone_number")
                )
        );

        employee.put(
                "salary",
                decryptionService.decrypt(
                        (String) employee.get("salary")
                )
        );

        employee.put(
                "emergency_contact_phone",
                decryptionService.decrypt(
                        (String) employee.get("emergency_contact_phone")
                )
        );
    }
}