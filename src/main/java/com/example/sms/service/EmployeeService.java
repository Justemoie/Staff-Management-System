package com.example.sms.service;

import com.example.sms.dto.request.AssignmentRequest;
import com.example.sms.dto.response.EmployeeResponse;
import java.util.List;

public interface EmployeeService {
    List<EmployeeResponse> searchEmployeesByInitials(String firstName, String lastName);

    EmployeeResponse addAssignmentToEmployee(Long employeeId, Long assignmentId);

    EmployeeResponse deleteAssignmentFromEmployee(Long employeeId, Long assignmentId);
}
