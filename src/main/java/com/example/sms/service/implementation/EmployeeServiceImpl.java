package com.example.sms.service.implementation;

import com.example.sms.dto.request.EmployeeRequest;
import com.example.sms.dto.response.EmployeeResponse;
import com.example.sms.entity.Employee;
import com.example.sms.mapper.AssignmentMapper;
import com.example.sms.mapper.EmployeeMapper;
import com.example.sms.repository.EmployeeRepository;
import com.example.sms.service.EmployeeService;
import java.util.List;

import com.example.sms.service.GenericService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmployeeServiceImpl implements
        GenericService<EmployeeResponse, EmployeeRequest, Long>, EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final AssignmentMapper assignmentMapper;

    public EmployeeServiceImpl(
            EmployeeRepository employeesRepository,
            EmployeeMapper employeeMapper,
            AssignmentMapper assignmentMapper) {

        this.employeeRepository = employeesRepository;
        this.assignmentMapper = assignmentMapper;
        this.employeeMapper = employeeMapper;
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
}
