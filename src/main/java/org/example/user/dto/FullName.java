package org.example.user.dto;

import jakarta.validation.constraints.NotBlank;

public record FullName (
        @NotBlank(message = "First name cannot be blank")
        String firstName,

        @NotBlank(message = "Last name cannot be blank")
        String lastName
) {
}
