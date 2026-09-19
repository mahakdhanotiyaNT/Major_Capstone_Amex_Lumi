package com.amex.lumi.beam.pipeline;

import com.amex.lumi.beam.cleansing.EmployeeCleansing;
import com.amex.lumi.beam.model.Employee;
import org.apache.beam.sdk.transforms.DoFn;

public class CleansingDoFn
        extends DoFn<Employee, Employee> {

    @ProcessElement
    public void processElement(ProcessContext context) {

        Employee employee = context.element();

        EmployeeCleansing cleansing =
                new EmployeeCleansing();

        Employee cleansedEmployee =
                cleansing.cleanse(employee);

        context.output(cleansedEmployee);
    }
}