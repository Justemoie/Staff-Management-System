package com.example.sms.dto.response;

import java.time.LocalDateTime;

public record FeedBackResponse(
        Long id,
        String comment,
        LocalDateTime createdAt
) {

}
