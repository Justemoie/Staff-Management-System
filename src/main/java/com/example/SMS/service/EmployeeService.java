package com.example.SMS.service;

import com.example.SMS.entity.Employee;
import com.example.SMS.repository.EmployeeRepository;
import org.springframework.data.crossstore.ChangeSetPersister;
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
        Employee savedEmployee = employeeRepository.save(employee);
        System.out.println("Saved Employee ID: " + savedEmployee.getId());

        return savedEmployee;
    }

    public Employee updateEmployee(Long id, Employee employeeData) throws ChangeSetPersister.NotFoundException {
        var targetEmployeeOptional = employeeRepository.findById(id);

//        if (targetEmployeeOptional.isEmpty()) {
//            throw new ChangeSetPersister.NotFoundException();
//        }

        var targetEmployee = targetEmployeeOptional.get();

        if (employeeData != null) {
            targetEmployee.setFirstName(employeeData.getFirstName());
            targetEmployee.setLastName(employeeData.getLastName());
            targetEmployee.setPhoneNumber(employeeData.getPhoneNumber());
            targetEmployee.setEmail(employeeData.getEmail());
            targetEmployee.setAssignments(employeeData.getAssignments());

            return employeeRepository.save(targetEmployee);
        }

        return targetEmployee;
    }

    public Optional<Employee> findByFirstName(String firstName) {
        return employeeRepository.findByFirstName(firstName);
    }

    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }
}
