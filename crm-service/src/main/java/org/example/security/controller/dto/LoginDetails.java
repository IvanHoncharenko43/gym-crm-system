package org.example.security.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for displaying login response details")
public record LoginDetails(
        @Schema(description = "Token issued to the user")
        String token
) {
}
