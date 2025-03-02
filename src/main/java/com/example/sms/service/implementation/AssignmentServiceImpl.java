package com.example.sms.service.implementation;

import com.example.sms.dto.request.AssignmentRequest;
import com.example.sms.dto.response.AssignmentResponse;
import com.example.sms.entity.Assignment;
import com.example.sms.entity.Employee;
import com.example.sms.mapper.AssignmentMapper;
import com.example.sms.repository.AssignmentRepository;
import com.example.sms.repository.EmployeeRepository;
import com.example.sms.service.AssignmentService;
import com.example.sms.service.GenericService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class AssignmentServiceImpl implements
        GenericService<AssignmentResponse, AssignmentRequest, Long>, AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentMapper assignmentMapper;
    private final EmployeeRepository employeeRepository;

    public AssignmentServiceImpl(
            AssignmentRepository assignmentRepository,
            AssignmentMapper assignmentMapper,
            EmployeeRepository employeeRepository) {

        this.assignmentRepository = assignmentRepository;
        this.assignmentMapper = assignmentMapper;
        this.employeeRepository = employeeRepository;
    }

    public List<AssignmentResponse> getAll() {
        return assignmentMapper.toAssignmentResponseList(assignmentRepository.findAll());
    }

    public AssignmentResponse getById(Long id) {
        return assignmentMapper.toAssignmentResponse(assignmentRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found")));
    }

    public AssignmentResponse create(AssignmentRequest assignmentRequest) {
        return assignmentMapper.toAssignmentResponse(
                assignmentRepository.save(assignmentMapper.toAssignment(assignmentRequest)));
    }

    public AssignmentResponse update(Long id, AssignmentRequest assignmentRequest) {
        Assignment assignment = assignmentRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));

        Assignment updatedAssignment = assignmentRepository.save(
                assignmentMapper.partialUpdate(assignmentRequest, assignment));

        return assignmentMapper.toAssignmentResponse(updatedAssignment);
    }

    public void delete(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Assignment not found with id = " + assignmentId));

        for (Employee employee : employeeRepository.findAll()) {
            employee.getAssignments().remove(assignment);
        }

        assignmentRepository.deleteById(assignmentId);
    }
}
