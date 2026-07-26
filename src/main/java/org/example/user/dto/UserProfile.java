package org.example.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserProfile (
        @NotBlank(message = "Username cannot be blank")
        String username
){
}
