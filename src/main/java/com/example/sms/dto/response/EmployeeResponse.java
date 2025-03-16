package com.example.sms.dto.response;

import java.util.List;

public record EmployeeResponse(
        Long id,
        String firstName,
        String lastName,
        String phoneNumber,
        String email,
        List<AssignmentResponse> assignments
) {
    public EmployeeResponse {
        if (id == null) id = 0L;
        if (firstName == null) firstName = "";
        if (lastName == null) lastName = "";
        if (phoneNumber == null) phoneNumber = "";
        if (email == null) email = "";
        if (assignments == null) assignments = List.of();
    }
}
