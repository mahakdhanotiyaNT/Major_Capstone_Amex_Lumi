package com.amex.lumi.ingestion.controller;

import com.amex.lumi.ingestion.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    void shouldGetAllEmployees() throws Exception {

        Map<String, Object> employee = new HashMap<>();

        employee.put("employee_id", "EMP1001");
        employee.put("first_name", "Aarav");
        employee.put("phone_number", "9876543210");
        employee.put("salary", "75000");

        when(employeeService.getAllEmployees())
                .thenReturn(List.of(employee));

        mockMvc.perform(
                        get("/api/v1/employees")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].employee_id")
                                .value("EMP1001")
                )
                .andExpect(
                        jsonPath("$[0].first_name")
                                .value("Aarav")
                )
                .andExpect(
                        jsonPath("$[0].phone_number")
                                .value("9876543210")
                )
                .andExpect(
                        jsonPath("$[0].salary")
                                .value("75000")
                );
    }

    @Test
    void shouldGetEmployeeById() throws Exception {

        Map<String, Object> employee = new HashMap<>();

        employee.put("employee_id", "EMP1001");
        employee.put("first_name", "Aarav");
        employee.put("phone_number", "9876543210");
        employee.put("salary", "75000");

        when(employeeService.getEmployeeById("EMP1001"))
                .thenReturn(employee);

        mockMvc.perform(
                        get("/api/v1/employees/EMP1001")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.employee_id")
                                .value("EMP1001")
                )
                .andExpect(
                        jsonPath("$.first_name")
                                .value("Aarav")
                )
                .andExpect(
                        jsonPath("$.phone_number")
                                .value("9876543210")
                )
                .andExpect(
                        jsonPath("$.salary")
                                .value("75000")
                );
    }

    @Test
    void shouldReturnNotFoundWhenEmployeeDoesNotExist()
            throws Exception {

        when(employeeService.getEmployeeById("EMP9999"))
                .thenReturn(null);

        mockMvc.perform(
                        get("/api/v1/employees/EMP9999")
                )
                .andExpect(status().isNotFound());
    }
}