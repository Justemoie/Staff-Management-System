package com.example.sms.service.implementation;

import com.example.sms.dto.request.EmployeeRequest;
import com.example.sms.dto.response.EmployeeResponse;
import com.example.sms.entity.Assignment;
import com.example.sms.entity.Employee;
import com.example.sms.mapper.EmployeeMapper;
import com.example.sms.repository.AssignmentRepository;
import com.example.sms.repository.EmployeeRepository;
import com.example.sms.service.EmployeeService;
import com.example.sms.service.GenericService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmployeeServiceImpl implements
        GenericService<EmployeeResponse, EmployeeRequest, Long>, EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final AssignmentRepository assignmentRepository;

    public EmployeeServiceImpl(
            EmployeeRepository employeesRepository,
            EmployeeMapper employeeMapper,
            AssignmentRepository assignmentRepository) {

        this.employeeRepository = employeesRepository;
        this.employeeMapper = employeeMapper;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public List<EmployeeResponse> getAll() {
        return employeeMapper.toEmployeeResponseList(employeeRepository.findAll());
    }

    @Override
    public EmployeeResponse getById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found with such id = " + id));

        return employeeMapper.toEmployeeResponse(employee);
    }

    @Override
    public EmployeeResponse create(EmployeeRequest employeeRequest) {
        return employeeMapper.toEmployeeResponse(
                employeeRepository.save(employeeMapper.toEmployee(employeeRequest)));
    }

    @Override
    public EmployeeResponse update(Long id, EmployeeRequest employeeRequest) {
        Employee employeeToUpdate = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found with id = " + id));

        Employee updatedEmployee = employeeRepository.save(
                employeeMapper.partialUpdate(employeeRequest, employeeToUpdate));

        return employeeMapper.toEmployeeResponse(updatedEmployee);
    }

    @Override
    public void delete(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Employee not found with id = " + id);
        }
        employeeRepository.deleteById(id);
    }

    @Override
    public List<EmployeeResponse> searchEmployeesByInitials(String firstName, String lastName) {
        if (firstName != null && lastName != null) {
            return employeeMapper.toEmployeeResponseList(
                    employeeRepository.findByFirstNameAndLastName(firstName, lastName));
        } else if (firstName != null) {
            return employeeMapper.toEmployeeResponseList(
                    employeeRepository.findByFirstName(firstName));
        } else if (lastName != null) {
            return employeeMapper.toEmployeeResponseList(
                    employeeRepository.findByLastName(lastName));
        } else {
            return employeeMapper.toEmployeeResponseList(employeeRepository.findAll());
        }
    }

    @Override
    public EmployeeResponse addAssignmentToEmployee(Long employeeId, Long assignmentId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found with such id = " + employeeId));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Assignment not found with id = " + assignmentId));

        if (employee.getAssignments().contains(assignment)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Employee with id = " + employeeId
                            + " already has assignment with id = " + assignmentId);
        }

        employee.getAssignments().add(assignment);

        return employeeMapper.toEmployeeResponse(employeeRepository.save(employee));
    }

    @Override
    public EmployeeResponse deleteAssignmentFromEmployee(Long employeeId, Long assignmentId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found with Id = " + employeeId));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Assignment not found with Id = " + assignmentId));

        if (!employee.getAssignments().contains(assignment)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Employee with id = " + employeeId
                            + " does not have assignment with id = " + assignmentId);
        }

        employee.getAssignments().remove(assignment);

        return employeeMapper.toEmployeeResponse(employeeRepository.save(employee));
    }
}
