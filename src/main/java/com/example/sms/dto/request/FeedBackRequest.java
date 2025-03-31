package com.example.sms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record FeedBackRequest(
        @NotBlank(message = "Comment must not be blank")
        @Size(max = 500, message = "Comment must not exceed 500 characters")
        String comment,

        @NotNull(message = "Creation date and time must not be null")
        LocalDateTime createdAt
) {

}
