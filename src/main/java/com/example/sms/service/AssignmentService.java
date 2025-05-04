package com.example.sms.service;

import com.example.sms.dto.request.AssignmentRequest;
import com.example.sms.dto.request.EmployeeRequest;
import com.example.sms.dto.request.FeedBackRequest;
import com.example.sms.dto.response.AssignmentResponse;
import com.example.sms.dto.response.EmployeeResponse;
import com.example.sms.dto.response.FeedBackResponse;

import java.util.List;

public interface AssignmentService {
    AssignmentResponse addFeedBack(Long assignmentId, FeedBackRequest feedBackRequest);

    AssignmentResponse deleteFeedBack(Long assignmentId, Long feedBackId);

    FeedBackResponse updateFeedBack(Long assignmentId,
                                    Long feedBackId,
                                    FeedBackRequest feedBackRequest);

    List<AssignmentResponse> getAll();

    AssignmentResponse getById(Long id);

    AssignmentResponse create(AssignmentRequest requestEntity);

    AssignmentResponse update(Long id, AssignmentRequest requestEntity);

    void delete(Long id);
}
