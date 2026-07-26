package org.example.training.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreateTrainingRequest(
        @NotBlank(message = "Trainer username cannot be blank")
        String trainerUsername,

        @NotBlank(message = "Trainee username cannot be blank")
        String traineeUsername,

        @NotBlank(message = "Training name cannot be blank")
        String trainingName,

        @NotNull(message = "Training date cannot be null")
        LocalDate trainingDate,

        @Positive(message = "Duration must be a positive number")
        int durationMinutes
) {
}
