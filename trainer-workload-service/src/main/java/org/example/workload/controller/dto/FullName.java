package org.example.workload.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO for full name", name = "FullName")
public record FullName(

        @Schema(description = "The first of the trainer", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "First name cannot be blank")
        String firstName,

        @Schema(description = "The last names of the trainer", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Last name cannot be blank")
        String lastName
) {
}
