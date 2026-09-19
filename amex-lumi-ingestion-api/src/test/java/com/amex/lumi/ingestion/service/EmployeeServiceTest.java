package com.amex.lumi.ingestion.service;

import com.amex.lumi.ingestion.exception.EmployeeNotFoundException;
import com.amex.lumi.ingestion.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DecryptionService decryptionService;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void shouldGetAllEmployees() {

        Map<String, Object> employee =
                new java.util.HashMap<>();

        employee.put("employee_id", "EMP1001");
        employee.put("phone_number", "encrypted-phone");
        employee.put("salary", "encrypted-salary");
        employee.put(
                "emergency_contact_phone",
                "encrypted-emergency-phone"
        );

        when(employeeRepository.findAll())
                .thenReturn(List.of(employee));

        when(decryptionService.decrypt("encrypted-phone"))
                .thenReturn("9876543210");

        when(decryptionService.decrypt("encrypted-salary"))
                .thenReturn("75000");

        when(decryptionService.decrypt("encrypted-emergency-phone"))
                .thenReturn("9123456789");

        List<Map<String, Object>> result =
                employeeService.getAllEmployees();

        assertEquals(1, result.size());
        assertEquals(
                "EMP1001",
                result.get(0).get("employee_id")
        );
        assertEquals(
                "9876543210",
                result.get(0).get("phone_number")
        );
        assertEquals(
                "75000",
                result.get(0).get("salary")
        );
        assertEquals(
                "9123456789",
                result.get(0).get("emergency_contact_phone")
        );
    }

    @Test
    void shouldGetEmployeeById() {

        Map<String, Object> employee =
                new java.util.HashMap<>();

        employee.put("employee_id", "EMP1001");
        employee.put("phone_number", "encrypted-phone");
        employee.put("salary", "encrypted-salary");
        employee.put(
                "emergency_contact_phone",
                "encrypted-emergency-phone"
        );

        when(employeeRepository.findByEmployeeId("EMP1001"))
                .thenReturn(employee);

        when(decryptionService.decrypt("encrypted-phone"))
                .thenReturn("9876543210");

        when(decryptionService.decrypt("encrypted-salary"))
                .thenReturn("75000");

        when(decryptionService.decrypt("encrypted-emergency-phone"))
                .thenReturn("9123456789");

        Map<String, Object> result =
                employeeService.getEmployeeById("EMP1001");

        assertEquals(
                "EMP1001",
                result.get("employee_id")
        );
        assertEquals(
                "9876543210",
                result.get("phone_number")
        );
        assertEquals(
                "75000",
                result.get("salary")
        );
        assertEquals(
                "9123456789",
                result.get("emergency_contact_phone")
        );
    }

    @Test
    void shouldThrowExceptionWhenEmployeeDoesNotExist() {

        when(employeeRepository.findByEmployeeId("EMP9999"))
                .thenReturn(null);

        EmployeeNotFoundException exception =
                assertThrows(
                        EmployeeNotFoundException.class,
                        () -> employeeService.getEmployeeById("EMP9999")
                );

        assertEquals(
                "Employee not found with ID: EMP9999",
                exception.getMessage()
        );
    }
}
