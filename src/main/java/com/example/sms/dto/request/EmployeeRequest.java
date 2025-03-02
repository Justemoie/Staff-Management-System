package com.example.sms.dto.request;

public record EmployeeRequest(
         String firstName,
         String lastName,
         String phoneNumber,
         String email
) { }
