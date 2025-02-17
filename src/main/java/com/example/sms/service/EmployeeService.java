package com.example.sms.service;

import com.example.sms.entity.Employee;
import com.example.sms.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeesRepository) {
        this.employeeRepository = employeesRepository;
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee addEmployee(Employee employee) {
        return  employeeRepository.save(employee);
    }

    public List<Employee> searchEmployees(String firstName, String lastName) {
        if (firstName != null && lastName != null) {
            return employeeRepository.findByFirstNameAndLastName(firstName, lastName);
        } else if (firstName != null) {
            return employeeRepository.findByFirstName(firstName);
        } else if (lastName != null) {
            return employeeRepository.findByLastName(lastName);
        } else {
            return employeeRepository.findAll();
        }
    }

    public List<Employee> findByFirstName(String firstName) {
        return employeeRepository.findByFirstName(firstName);
    }

    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }
}
