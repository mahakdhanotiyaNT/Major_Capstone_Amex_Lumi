package com.amex.lumi.ingestion.controller;

import com.amex.lumi.ingestion.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllEmployees() {
        return ResponseEntity.ok(
                employeeService.getAllEmployees()
        );
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<Map<String, Object>> getEmployeeById(
            @PathVariable String employeeId) {

        Map<String, Object> employee =
                employeeService.getEmployeeById(employeeId);

        if (employee == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(employee);
    }
}