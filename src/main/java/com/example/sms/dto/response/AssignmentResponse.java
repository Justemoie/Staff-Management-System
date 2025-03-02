package com.example.sms.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

public record AssignmentResponse (
        Long id,
        String title,
        String description,
        @JsonIgnore List<EmployeeResponse> employees
) {}
