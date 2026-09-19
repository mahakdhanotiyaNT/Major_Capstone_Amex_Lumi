package com.amex.lumi.ingestion.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class EmployeeRepository {

    private final JdbcTemplate jdbcTemplate;

    public EmployeeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> findAll() {
        String sql = """
                SELECT *
                FROM employees
                """;

        return jdbcTemplate.queryForList(sql);
    }

    public Map<String, Object> findByEmployeeId(String employeeId) {
        String sql = """
                SELECT *
                FROM employees
                WHERE employee_id = ?
                """;

        List<Map<String, Object>> results =
                jdbcTemplate.queryForList(sql, employeeId);

        if (results.isEmpty()) {
            return null;
        }

        return results.get(0);
    }
}