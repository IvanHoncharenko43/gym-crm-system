package org.example.workload.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.workload.controller.dto.ActionType;
import org.example.workload.controller.dto.FullName;

import java.time.LocalDate;

@Schema(description = "DTO for updating trainer's training workload", name = "TrainerWorkload")
public record TrainerWorkloadRequest(
        @Schema(description = "Username of the trainer", example = "John.Doe", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Username cannot be blank")
        String username,

        @Schema(description = "The first and last names of the trainer", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Full name cannot be null")
        @Valid
        FullName fullName,

        @Schema(description = "Activity status of the trainer")
        @NotNull(message = "Activity status cannot be null")
        boolean isActive,

        @Schema(description = "Date of the trainer's training")
        @NotNull(message = "Training date cannot be null")
        LocalDate trainingDate,

        @Schema(description = "Duration of the training in minutes", requiredMode = Schema.RequiredMode.REQUIRED)
        @Positive(message = "Duration must be a positive number")
        @Min(value = 20, message = "Training duration must be at least 20 minutes")
        @Max(value = 360, message = "Training duration cannot exceed 6 hours (360 minutes)")
        int trainingSummaryDurationMinutes,

        @Schema(description = "Action type of the request")
        @NotNull(message = "Action type cannot be null")
        ActionType actionType
) {
}
