package com.example.sms.service;

import com.example.sms.entity.Assignment;
import com.example.sms.entity.Employee;
import com.example.sms.repository.AssignmentRepository;
import com.example.sms.repository.EmployeeRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final AssignmentRepository assignmentRepository;

    public EmployeeService(
            EmployeeRepository employeesRepository, AssignmentRepository assignmentRepository) {

        this.employeeRepository = employeesRepository;
        this.assignmentRepository = assignmentRepository;
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


    public Employee findById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found with id: " + id));
    }


    public Employee updateEmployee(Long id, Employee employeeDetails) {
        Optional<Employee> existingEmployeeOpt = employeeRepository.findById(id);

        if (existingEmployeeOpt.isPresent()) {
            Employee existingEmployee = existingEmployeeOpt.get();

            existingEmployee.setFirstName(employeeDetails.getFirstName());
            existingEmployee.setLastName(employeeDetails.getLastName());
            existingEmployee.setEmail(employeeDetails.getEmail());
            existingEmployee.setPhoneNumber(employeeDetails.getPhoneNumber());

            return employeeRepository.save(existingEmployee);
        } else {
            throw new IllegalArgumentException("Employee with ID " + id + " not found");
        }
    }

    public Employee deleteEmployee(Long id) {
        Optional<Employee> existingEmployeeOpt = employeeRepository.findById(id);

        if (existingEmployeeOpt.isPresent()) {
            Employee existingEmployee = existingEmployeeOpt.get();

            for (Assignment assignment : existingEmployee.getAssignments()) {
                for (Employee employee : assignment.getEmployees()) {
                    assignment.getEmployees().remove(employee);
                    assignmentRepository.save(assignment);
                }
            }

            employeeRepository.delete(existingEmployee);

            return existingEmployee;
        } else {
            throw new IllegalArgumentException("Employee with ID " + id + " not found");
        }
    }
}
