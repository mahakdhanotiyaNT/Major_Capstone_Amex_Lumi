package com.amex.lumi.beam.pipeline;

import com.amex.lumi.beam.model.Employee;
import com.amex.lumi.beam.validation.EmployeeValidator;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.TupleTag;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ValidateEmployeeDoFn
        extends DoFn<Employee, Employee> {

    private static final Logger log =
            LoggerFactory.getLogger(ValidateEmployeeDoFn.class);

    public static final TupleTag<String> INVALID_RECORDS =
            new TupleTag<>("invalidRecords");

    @ProcessElement
    public void processElement(ProcessContext context) {

        Employee employee = context.element();

        EmployeeValidator validator =
                new EmployeeValidator();

        List<String> errors =
                validator.validate(employee);

        if (errors.isEmpty()) {
            log.info("Employee validation successful. Employee ID: {}",
                    employee.getEmployeeId()
            );

            context.output(employee);

        } else {

            log.warn("Employee validation failed. Employee ID: {}. Reason: {}",
                    employee.getEmployeeId(),
                    String.join(" ; ", errors)
            );

            String errorRecord =
                    "VALIDATION_ERROR"
                    + " | employee_id="
                    + employee.getEmployeeId()
                    + " | "
                    + String.join(" ; ", errors);

            context.output(
                    INVALID_RECORDS,
                    errorRecord
            );
        }
    }
}