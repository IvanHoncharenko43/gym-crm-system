package org.example.security.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO for making a login request")
public record LoginRequest(
        @Schema(description = "Username of the user")
        @NotBlank(message = "Username cannot be blank")
        String username,

        @Schema(description = "Password of the user")
        @NotBlank(message = "Password cannot be blank")
        String password
) {
}
