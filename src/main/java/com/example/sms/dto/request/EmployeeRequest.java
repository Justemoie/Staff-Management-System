package com.example.sms.dto.request;

public record EmployeeRequest(
         Long id,
         String firstName,
         String lastName,
         String phoneNumber,
         String email
) { }
