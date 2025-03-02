package com.example.sms.dto.response;

import java.util.List;

public record EmployeeResponse(
        Long id,
        String firstName,
        String lastName,
        String phoneNumber,
        String email,
        List<AssignmentResponse> assignments
) { }
