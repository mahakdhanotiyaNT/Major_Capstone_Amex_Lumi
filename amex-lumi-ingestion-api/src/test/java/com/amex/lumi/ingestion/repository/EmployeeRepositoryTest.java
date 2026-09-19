package com.amex.lumi.ingestion.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private EmployeeRepository employeeRepository;

    @Test
    void shouldFindAllEmployees() {

        List<Map<String, Object>> employees =
                List.of(
                        Map.of(
                                "employee_id",
                                "EMP1001",
                                "first_name",
                                "John"
                        )
                );

        when(jdbcTemplate.queryForList(
                "SELECT *\nFROM employees\n"
        )).thenReturn(employees);

        List<Map<String, Object>> result =
                employeeRepository.findAll();

        assertEquals(1, result.size());
        assertEquals(
                "EMP1001",
                result.get(0).get("employee_id")
        );
    }

    @Test
    void shouldFindEmployeeById() {

        Map<String, Object> employee =
                Map.of(
                        "employee_id",
                        "EMP1001",
                        "first_name",
                        "John"
                );

        when(jdbcTemplate.queryForList(
                "SELECT *\nFROM employees\nWHERE employee_id = ?\n",
                "EMP1001"
        )).thenReturn(List.of(employee));

        Map<String, Object> result =
                employeeRepository.findByEmployeeId("EMP1001");

        assertEquals(
                "EMP1001",
                result.get("employee_id")
        );
    }

    @Test
    void shouldReturnNullWhenEmployeeDoesNotExist() {

        when(jdbcTemplate.queryForList(
                "SELECT *\nFROM employees\nWHERE employee_id = ?\n",
                "UNKNOWN"
        )).thenReturn(List.of());

        Map<String, Object> result =
                employeeRepository.findByEmployeeId("UNKNOWN");

        assertNull(result);
    }
}
