package com.example.sms.service;

import com.example.sms.dto.request.AssignmentRequest;
import com.example.sms.dto.response.AssignmentResponse;
import java.util.List;

public interface AssignmentService {
    List<AssignmentResponse> getAllAssignments();

    AssignmentResponse getAssignmentById(Long id);

    AssignmentResponse createAssignment(AssignmentRequest assignmentRequest);

    AssignmentResponse updateAssignment(Long id, AssignmentRequest assignmentRequest);

    void deleteAssignment(Long id);
}
