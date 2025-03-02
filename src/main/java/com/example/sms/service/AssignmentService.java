package com.example.sms.service;

import com.example.sms.dto.request.FeedBackRequest;
import com.example.sms.dto.response.AssignmentResponse;

public interface AssignmentService {
    AssignmentResponse addFeedBack(Long assignmentId, FeedBackRequest feedBackRequest);

    AssignmentResponse deleteFeedBack(Long assignmentId, Long feedBackId);
}
