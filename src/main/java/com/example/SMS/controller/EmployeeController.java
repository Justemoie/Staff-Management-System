package com.example.SMS.controller;

import com.example.SMS.dto.EmployeeDto;
import com.example.SMS.entity.Employee;
import com.example.SMS.repository.EmployeeRepository;
import com.example.SMS.service.EmployeeService;
import org.apache.coyote.BadRequestException;
import org.hibernate.annotations.NotFound;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

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
                .map(employee ->
                { if (employee == null) {
                        return null;
                    } else {
                        return new EmployeeDto(employee, false);
                    }
                }).toList();
    }

    @GetMapping("/{firstName}")
    public EmployeeDto getEmployeeByUsername(@PathVariable(name = "firstName") String firstName) {
        return new EmployeeDto(
                employeeService.findByFirstName(firstName).orElseThrow(),
                true
        );
    }

    @GetMapping("/employees/{id}")
    EmployeeDto getEmployeeById(@PathVariable Long id) {
        return new EmployeeDto(
                employeeService.findById(id).orElseThrow(),
                true
        );
    }

    @PostMapping
    public EmployeeDto createEmployee(@RequestBody Employee employee) {
        return new EmployeeDto(employeeService.addEmployee(employee), false);
    }

    @PutMapping("/{id}")
    public EmployeeDto updateEmployee(@PathVariable Long id, @RequestBody Employee employeeData) {
        try {
            return new EmployeeDto(employeeService.updateEmployee(id, employeeData), true);
        } catch (ChangeSetPersister.NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found", e);
        }
    }
}
