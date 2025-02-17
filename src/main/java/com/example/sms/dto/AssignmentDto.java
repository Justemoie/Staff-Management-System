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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<EmployeeDto> getEmployees() {
        return employees;
    }

    public void setEmployees(List<EmployeeDto> employees) {
        this.employees = employees;
    }
}
