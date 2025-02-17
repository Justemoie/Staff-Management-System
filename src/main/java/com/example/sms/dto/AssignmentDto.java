package com.example.sms.dto;

import com.example.sms.entity.Assignment;

import java.util.ArrayList;
import java.util.List;

public class AssignmentDto {
    private Long id;

    private String title;
    private String description;

    private List<EmployeeDto> employees;

    public AssignmentDto(Assignment assignment, boolean includeEmployees) {
        this.id = assignment.getId();
        this.title = assignment.getTitle();
        this.description = assignment.getDescription();

        employees = new ArrayList<>();

        if (includeEmployees) {
            this.employees = assignment.getEmployees().stream()
                    .map(employee -> new EmployeeDto(employee, false))
                    .toList();
        }
    }
}
