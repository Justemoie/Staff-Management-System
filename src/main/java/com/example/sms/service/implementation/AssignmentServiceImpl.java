package com.example.sms.service.implementation;

import com.example.sms.dto.request.AssignmentRequest;
import com.example.sms.dto.response.AssignmentResponse;
import com.example.sms.entity.Assignment;
import com.example.sms.mapper.AssignmentMapper;
import com.example.sms.repository.AssignmentRepository;
import com.example.sms.service.AssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class AssignmentServiceImpl implements AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final AssignmentMapper assignmentMapper;

    public AssignmentServiceImpl(
            AssignmentRepository assignmentRepository, AssignmentMapper assignmentMapper) {

        this.assignmentRepository = assignmentRepository;
        this.assignmentMapper = assignmentMapper;
    }

    public List<AssignmentResponse> getAllAssignments() {
        return assignmentMapper.toAssignmentResponseList(assignmentRepository.findAll());
    }

    public AssignmentResponse getAssignmentById(Long id) {
        return assignmentMapper.toAssignmentResponse(assignmentRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found")));
    }

    public AssignmentResponse createAssignment(AssignmentRequest assignmentRequest) {
        return assignmentMapper.toAssignmentResponse(
                assignmentRepository.save(assignmentMapper.toAssignment(assignmentRequest)));
    }

    public AssignmentResponse updateAssignment(Long id, AssignmentRequest assignmentRequest) {
        Assignment assignment = assignmentRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));

        Assignment updatedAssignment = assignmentRepository.save(
                assignmentMapper.partialUpdate(assignmentRequest, assignment));

        return assignmentMapper.toAssignmentResponse(updatedAssignment);
    }

    public void deleteAssignment(Long id) {
        assignmentRepository.deleteById(id);
    }
}
