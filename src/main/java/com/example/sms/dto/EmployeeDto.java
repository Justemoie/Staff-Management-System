package com.example.sms.dto;

import com.example.sms.entity.Employee;
import jdk.jfr.DataAmount;

import java.util.ArrayList;
import java.util.List;

@DataAmount
public class EmployeeDto {
    private Long id;

    private String firstName;
    private String lastName;
    private String phone;
    private String email;

    private List<AssignmentDto> assignments;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<AssignmentDto> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<AssignmentDto> assignments) {
        this.assignments = assignments;
    }
}
