package com.example.sms.dto.response;

import java.util.List;

public record AssignmentResponse(
        Long id,
        String title,
        String description,
        List<FeedBackResponse> feedBacks
) {
    
}
