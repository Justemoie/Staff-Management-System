package com.example.sms.controller;

import com.example.sms.dto.request.EmployeeRequest;
import com.example.sms.dto.response.EmployeeResponse;
import com.example.sms.service.EmployeeService;
import com.example.sms.service.GenericService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("employees")
@Tag(name = "Employee Management")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final GenericService<EmployeeResponse, EmployeeRequest, Long> genericService;

    public EmployeeController(
            EmployeeService employeeService,
            GenericService<EmployeeResponse, EmployeeRequest, Long> genericService) {
        this.employeeService = employeeService;
        this.genericService = genericService;
    }

    @GetMapping("/all")
    @Operation(summary = "Get all employees")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        return ResponseEntity.ok(genericService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(genericService.getById(id));
    }

    @PostMapping("/create")
    @Operation(summary = "Create an employee")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody EmployeeRequest employeeRequest) {
        return ResponseEntity.ok(genericService.create(employeeRequest));
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
        return ResponseEntity.ok(genericService.update(id, employeeRequest));
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete an employee")
    public void deleteEmployee(@PathVariable Long id) {
        genericService.delete(id);
    }

    @GetMapping("/search-by-first-name")
    @Operation(summary = "Search by first name")
    public ResponseEntity<List<EmployeeResponse>> searchByFirstName(
            @RequestParam String firstName) {
        return ResponseEntity.ok(employeeService.searchEmployeesByFirstName(firstName));
    }

    @GetMapping("/search-by-last-name")
    @Operation(summary = "Search by last name")
    public ResponseEntity<List<EmployeeResponse>> searchEmployees(
            @RequestParam(name = "lastName", required = false) String lastName) {
        return ResponseEntity.ok(employeeService.searchEmployeesByLastName(lastName));
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