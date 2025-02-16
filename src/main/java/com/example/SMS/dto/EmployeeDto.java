package com.example.SMS.dto;

import com.example.SMS.entity.Employee;
import jdk.jfr.DataAmount;

import java.util.ArrayList;
import java.util.List;

@DataAmount
public class EmployeeDto {
    public Long id;

    public String firstName;
    public String lastName;
    public String phone;
    public String email;

    public List<AssignmentDto> assignments;

    public EmployeeDto(Employee employee, boolean includeAssignments) {
        this.id = employee.getId();
        this.firstName = employee.getFirstName();
        this.lastName = employee.getLastName();
        this.phone = employee.getPhoneNumber();
        this.email = employee.getEmail();

        assignments = new ArrayList<>();

        if (includeAssignments) {
            this.assignments = employee.getAssignments().stream()
                    .map(assignment -> new AssignmentDto(assignment, false))
                    .toList();
        }
    }
}
