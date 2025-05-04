package com.example.sms.controller;

import com.example.sms.dto.request.EmployeeRequest;
import com.example.sms.dto.response.EmployeeResponse;
import com.example.sms.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("employees")
@Tag(name = "Employee Management")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(
            EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/all")
    @Operation(summary = "Get all employees")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @PostMapping("/create")
    @Operation(summary = "Create an employee")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody EmployeeRequest employeeRequest) {
        return ResponseEntity.ok(employeeService.create(employeeRequest));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Upsert a list of employees")
    public ResponseEntity<List<EmployeeResponse>> upsertEmployees(
            @Valid @RequestBody List<EmployeeRequest> employeeRequests) {
        List<EmployeeResponse> responses = employeeService.bulkUpsertEmployees(employeeRequests);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "Update an employee")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest employeeRequest) {
        return ResponseEntity.ok(employeeService.update(id, employeeRequest));
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete an employee")
    public void deleteEmployee(@PathVariable Long id) {
        employeeService.delete(id);
    }

    @GetMapping("/employees/search")
    public ResponseEntity<List<EmployeeResponse>> searchEmployees(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName) {
        List<EmployeeResponse> employees = employeeService.searchByInitials(firstName, lastName);
        return ResponseEntity.ok(employees);
    }

    @PostMapping("/{employeeId}/add-assignment/{assignmentId}")
    @Operation(summary = "Add assignment to employee")
    public ResponseEntity<EmployeeResponse> addAssignment(
            @PathVariable Long employeeId, @PathVariable Long assignmentId) {
        return ResponseEntity.ok(employeeService.addAssignmentToEmployee(employeeId, assignmentId));
    }

    @DeleteMapping("/{employeeId}/delete-assignment/{assignmentId}")
    @Operation(summary = "Remove assignment from employee")
    public void deleteAssignment(@PathVariable Long employeeId, @PathVariable Long assignmentId) {
        employeeService.deleteAssignmentFromEmployee(employeeId, assignmentId);
    }

    @GetMapping("/search-by-assignment/{id}")
    @Operation(summary = "Search by assignment ID")
    public ResponseEntity<List<EmployeeResponse>> searchByAssignmentTitle(
            @PathVariable Long id) {
        return ResponseEntity.ok(employeeService.searchEmployeesByAssignmentId(id));
    }
}