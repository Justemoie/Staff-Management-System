package com.example.sms.service;

import com.example.sms.dto.response.EmployeeResponse;
import java.util.List;

public interface EmployeeService {
    List<EmployeeResponse> searchEmployeesByFirstName(String firstName);

    List<EmployeeResponse> searchEmployeesByLastName(String lastName);

    List<EmployeeResponse> searchEmployeesByAssignmentId(Long assignmentId);

    EmployeeResponse addAssignmentToEmployee(Long employeeId, Long assignmentId);

    EmployeeResponse deleteAssignmentFromEmployee(Long employeeId, Long assignmentId);
}
