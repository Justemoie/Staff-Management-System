package com.example.sms.dto.response;

public record ErrorResponse(
      String message,
      String details,
      int status
) {
}
