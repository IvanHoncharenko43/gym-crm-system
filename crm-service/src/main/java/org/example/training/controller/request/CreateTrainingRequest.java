package org.example.training.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import org.example.core.validator.ValidMinDaysInAdvance;

import java.time.LocalDate;

@Schema(description = "DTO for adding a new training", name = "CreateTraining")
public record CreateTrainingRequest(

        @Schema(description = "Username of the trainer participated in the training", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Trainer username cannot be blank")
        String trainerUsername,

        @Schema(description = "Username of the trainee participated in the training", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Trainee username cannot be blank")
        String traineeUsername,

        @Schema(description = "Name of the training", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Training name cannot be blank")
        String trainingName,

        @Schema(description = "Date of the training", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Training date cannot be null")
        @ValidMinDaysInAdvance
        LocalDate trainingDate,

        @Schema(description = "Duration of the training in minutes", requiredMode = Schema.RequiredMode.REQUIRED)
        @Positive(message = "Duration must be a positive number")
        @Min(value = 20, message = "Training duration must be at least 20 minutes")
        @Max(value = 360, message = "Training duration cannot exceed 6 hours (360 minutes)")
        int durationMinutes
) {
}
