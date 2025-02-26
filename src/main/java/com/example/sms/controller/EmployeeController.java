package com.example.sms.controller;

import com.example.sms.dto.EmployeeDto;
import com.example.sms.entity.Employee;
import com.example.sms.service.EmployeeService;
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
@RequestMapping("api")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/employees")
    public List<EmployeeDto> getAllEmployees() {
        return employeeService.getAllEmployees().stream()
                .map(employee -> {
                    if (employee == null) {
                        return null;
                    } else {
                        return new EmployeeDto(employee, false);
                    }
                }).toList();
    }

    @GetMapping("/employees/{id}")
    ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long id) {
        EmployeeDto employee = new EmployeeDto(
                employeeService.findById(id), true
        );

        return ResponseEntity.ok(employee);
    }

    @GetMapping("/employees/search")
    public List<EmployeeDto> search(
            @RequestParam(name = "firstName", required = false) String firstName,
            @RequestParam(name = "lastName", required = false) String lastName) {

        List<Employee> employees = employeeService.searchEmployees(firstName, lastName);
        return employees.stream()
                .map(e -> new EmployeeDto(e, false))
                .toList();
    }

    @PostMapping
    public EmployeeDto createEmployee(@RequestBody Employee employee) {
        return new EmployeeDto(employeeService.addEmployee(employee), false);
    }

    @PutMapping("/employees/{id}")
    public ResponseEntity<EmployeeDto> updateEmployee(
            @PathVariable Long id, @RequestBody Employee employeeRequest) {

        EmployeeDto updatedEmployee = new EmployeeDto(
                employeeService.updateEmployee(id, employeeRequest), false);

        return ResponseEntity.ok(updatedEmployee);
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<EmployeeDto> deleteEmployee(@PathVariable Long id) {
        EmployeeDto deletedEmployee = new EmployeeDto(employeeService.deleteEmployee(id),
                false);

        return ResponseEntity.ok(deletedEmployee);
    }
}
