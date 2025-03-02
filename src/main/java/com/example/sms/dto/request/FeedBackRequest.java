package com.example.sms.dto.request;

import java.time.LocalDateTime;

public record FeedBackRequest(
     String comment,
     String author,
     LocalDateTime createdAt
) {
    
}
