package com.example.sms.controller;

import com.example.sms.dto.request.EmployeeRequest;
import com.example.sms.dto.response.EmployeeResponse;
import com.example.sms.service.EmployeeService;
import com.example.sms.service.GenericService;
import java.util.List;

import jakarta.validation.Valid;
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
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        return ResponseEntity.ok(genericService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(genericService.getById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody EmployeeRequest employeeRequest) {
        return ResponseEntity.ok(genericService.create(employeeRequest));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest employeeRequest) {

        return ResponseEntity.ok(genericService.update(id, employeeRequest));
    }

    @DeleteMapping("/delete/{id}")
    public void deleteEmployee(@PathVariable Long id) {
        genericService.delete(id);
    }

    @GetMapping("/search-by-first-name")
    public ResponseEntity<List<EmployeeResponse>> searchByFirstName(
            @RequestParam String firstName) {

        return ResponseEntity.ok(employeeService.searchEmployeesByFirstName(firstName));
    }

    @GetMapping("/search-by-last-name")
    public ResponseEntity<List<EmployeeResponse>> searchEmployees(
            @RequestParam(name = "lastName", required = false) String lastName) {

        return ResponseEntity.ok(employeeService.searchEmployeesByLastName(lastName));
    }

    @PostMapping("/{employeeId}/add-assignment/{assignmentId}")
    public ResponseEntity<EmployeeResponse> addAssignment(
            @PathVariable Long employeeId, @PathVariable Long assignmentId) {
        return ResponseEntity.ok(employeeService.addAssignmentToEmployee(employeeId, assignmentId));
    }

    @DeleteMapping("/{employeeId}/delete-assignment/{assignmentId}")
    public void deleteAssignment(@PathVariable Long employeeId, @PathVariable Long assignmentId) {
        employeeService.deleteAssignmentFromEmployee(employeeId, assignmentId);
    }

    @GetMapping("/search-by-assignment/{id}")
    public ResponseEntity<List<EmployeeResponse>> searchByAssignmentTitle(
            @PathVariable Long id) {

        return ResponseEntity.ok(employeeService.searchEmployeesByAssignmentId(id));
    }
}
