package com.example.sms.dto.request;

import java.time.LocalDateTime;

public record FeedBackRequest(
     String comment,
     LocalDateTime createdAt
) {
    
}
