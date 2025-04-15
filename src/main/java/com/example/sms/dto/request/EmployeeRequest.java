package com.example.sms.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmployeeRequest(
        @NotBlank(message = "First name must not be blank")
        @Size(min = 2, message = "First name must have at least 2 characters")
        String firstName,

        @NotBlank(message = "Last name must not be blank")
        String lastName,

        @NotBlank(message = "Phone number must not be empty")
        @Pattern(
                regexp = "^\\+375\\d{2}\\d{7}$",
                message = "Phone number must be in the format +375xx1111111, e.g., +375291234567"
        )
        String phoneNumber,

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email must not be blank")
        String email
) {

}
