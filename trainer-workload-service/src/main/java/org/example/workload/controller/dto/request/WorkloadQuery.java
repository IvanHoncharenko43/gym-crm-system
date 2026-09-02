package org.example.workload.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Query filters for a trainer's monthly workload", name = "WorkloadQuery")
public record WorkloadQuery(
        @Schema(description = "Username of the trainer", example = "John.Doe", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Username cannot be blank")
        String username,

        @Schema(description = "Year of the requested summary", example = "2026", requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(value = 2000, message = "Year must be 2000 or later")
        int year,

        @Schema(description = "Month of the requested summary (1-12)", example = "8", requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(value = 1, message = "Month must be between 1 and 12")
        @Max(value = 12, message = "Month must be between 1 and 12")
        int month
) {
}
