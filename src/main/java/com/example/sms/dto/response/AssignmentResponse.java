package com.example.sms.dto.response;

import java.util.List;

public record AssignmentResponse(
        Long id,
        String title,
        String description,
        List<FeedBackResponse> feedBacks
) {
    public AssignmentResponse {
        if (id == null) id = 0L;
        if (title == null) title = "";
        if (description == null) description = "";
        if (feedBacks == null) feedBacks = List.of();
    }
}
