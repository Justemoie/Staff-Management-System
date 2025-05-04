package com.example.sms.service;

import com.example.sms.dto.request.EmployeeRequest;
import com.example.sms.dto.response.EmployeeResponse;
import java.util.List;

public interface EmployeeService {
    List<EmployeeResponse> searchEmployeesByFirstName(String firstName);

    List<EmployeeResponse> searchEmployeesByLastName(String lastName);

    List<EmployeeResponse> searchEmployeesByAssignmentId(Long assignmentId);

    EmployeeResponse addAssignmentToEmployee(Long employeeId, Long assignmentId);

    EmployeeResponse deleteAssignmentFromEmployee(Long employeeId, Long assignmentId);

    List<EmployeeResponse> bulkUpsertEmployees(List<EmployeeRequest> employeeRequests);

    List<EmployeeResponse> getAll();

    EmployeeResponse getById(Long id);

    EmployeeResponse create(EmployeeRequest requestEntity);

    EmployeeResponse update(Long id, EmployeeRequest requestEntity);

    void delete(Long id);

    List<EmployeeResponse> searchByInitials(String firstName, String lastName);
}
