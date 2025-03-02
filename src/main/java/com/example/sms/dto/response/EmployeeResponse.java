package com.example.sms.dto.response;

public record EmployeeResponse(
        Long id,
        String firstName,
        String lastName,
        String phoneNumber,
        String email
) { }
