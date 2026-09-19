package com.amex.lumi.beam.pipeline;

import com.amex.lumi.beam.config.DatabaseConfig;
import com.amex.lumi.beam.config.DatabaseInitializer;
import com.amex.lumi.beam.model.Employee;
import org.apache.beam.sdk.transforms.DoFn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;

public class WriteToPostgresDoFn
        extends DoFn<Employee, Void> {

    private transient Connection connection;

    @Setup
    public void setup() throws Exception {

        DatabaseInitializer.initialize();
        connection = DriverManager.getConnection(
                DatabaseConfig.getJdbcUrl(),
                DatabaseConfig.getUsername(),
                DatabaseConfig.getPassword()
        );
    }

    @ProcessElement
    public void processElement(ProcessContext context)
            throws SQLException {

        Employee employee = context.element();

        String sql = """
                INSERT INTO employees (
                    employee_id,
                    first_name,
                    last_name,
                    email,
                    phone_number,
                    hire_date,
                    department,
                    job_title,
                    salary,
                    currency,
                    employment_status,
                    manager_id,
                    is_active,
                    skills,
                    address_street,
                    address_city,
                    address_state,
                    address_postal_code,
                    address_country,
                    emergency_contact_name,
                    emergency_contact_relationship,
                    emergency_contact_phone,
                    emergency_contact_email,
                    ingestion_timestamp,
                    execution_id,
                    source_creation_time
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?, ?, ?, ?, ?
                )
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, employee.getEmployeeId());
            statement.setString(2, employee.getFirstName());
            statement.setString(3, employee.getLastName());
            statement.setString(4, employee.getEmail());
            statement.setString(5, employee.getPhoneNumber());

            if (employee.getHireDate() == null
                    || employee.getHireDate().isBlank()) {

                statement.setNull(
                        6,
                        java.sql.Types.DATE
                );

            } else {

                statement.setDate(
                        6,
                        java.sql.Date.valueOf(
                                employee.getHireDate()
                        )
                );
            }

            statement.setString(7, employee.getDepartment());
            statement.setString(8, employee.getJobTitle());
            statement.setString(9, employee.getSalary());
            statement.setString(10, employee.getCurrency());
            statement.setString(11, employee.getEmploymentStatus());
            statement.setString(12, employee.getManagerId());

            if (employee.getIsActive() != null) {

                statement.setBoolean(
                        13,
                        employee.getIsActive()
                );

            } else {

                statement.setNull(
                        13,
                        java.sql.Types.BOOLEAN
                );
            }

            statement.setString(
                    14,
                    employee.getSkills() == null
                            ? null
                            : String.join(
                            "|",
                            employee.getSkills()
                    )
            );

            if (employee.getAddress() != null) {

                statement.setString(
                        15,
                        employee.getAddress().getStreet()
                );

                statement.setString(
                        16,
                        employee.getAddress().getCity()
                );

                statement.setString(
                        17,
                        employee.getAddress().getState()
                );

                statement.setString(
                        18,
                        employee.getAddress().getPostalCode()
                );

                statement.setString(
                        19,
                        employee.getAddress().getCountry()
                );

            } else {

                for (int i = 15; i <= 19; i++) {
                    statement.setNull(
                            i,
                            java.sql.Types.VARCHAR
                    );
                }
            }

            if (employee.getEmergencyContact() != null) {

                statement.setString(
                        20,
                        employee.getEmergencyContact().getName()
                );

                statement.setString(
                        21,
                        employee.getEmergencyContact().getRelationship()
                );

                statement.setString(
                        22,
                        employee.getEmergencyContact().getPhone()
                );

                statement.setString(
                        23,
                        employee.getEmergencyContact().getEmail()
                );

            } else {

                for (int i = 20; i <= 23; i++) {
                    statement.setNull(
                            i,
                            java.sql.Types.VARCHAR
                    );
                }
            }

            if (employee.getIngestionTimestamp() == null
                    || employee.getIngestionTimestamp().isBlank()) {

                statement.setNull(
                        24,
                        java.sql.Types.TIMESTAMP
                );

            } else {

                statement.setTimestamp(
                        24,
                        java.sql.Timestamp.from(
                                Instant.parse(
                                        employee.getIngestionTimestamp()
                                )
                        )
                );
            }

            statement.setString(
                    25,
                    employee.getExecutionId()
            );

            if (employee.getSourceCreationTime() == null
                    || employee.getSourceCreationTime().isBlank()) {

                statement.setNull(
                        26,
                        java.sql.Types.TIMESTAMP
                );

            } else {

                statement.setTimestamp(
                        26,
                        java.sql.Timestamp.from(
                                Instant.parse(
                                        employee.getSourceCreationTime()
                                )
                        )
                );
            }

            statement.executeUpdate();
        }
    }

    @Teardown
    public void teardown() throws SQLException {

        if (connection != null
                && !connection.isClosed()) {

            connection.close();
        }
    }
}