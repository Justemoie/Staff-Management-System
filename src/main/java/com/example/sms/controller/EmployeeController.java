package com.example.sms.controller;

import com.example.sms.dto.EmployeeDto;
import com.example.sms.entity.Employee;
import com.example.sms.service.EmployeeService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{firstName}")
    public EmployeeDto getEmployeeByUsername(@PathVariable(name = "firstName") String firstName) {
        return new EmployeeDto(employeeService.findByFirstName(firstName)
                .stream()
                .findFirst()
                .orElseThrow(),
                true);
    }

    @GetMapping("/employees/{id}")
    EmployeeDto getEmployeeById(@PathVariable Long id) {
        return new EmployeeDto(
                employeeService.findById(id).orElseThrow(),
                true
        );
    }

    @GetMapping("/search")
    public List<EmployeeDto> searchEmployees(@RequestParam(name = "firstName", required = false) String firstName,
                                             @RequestParam(name = "lastName", required = false) String lastName) {
        List<Employee> employees = employeeService.searchEmployees(firstName, lastName);
        return employees.stream()
                .map(employee -> new EmployeeDto(employee, false))
                .toList();
    }

    @PostMapping
    public EmployeeDto createEmployee(@RequestBody Employee employee) {
        return new EmployeeDto(employeeService.addEmployee(employee), false);
    }
}
