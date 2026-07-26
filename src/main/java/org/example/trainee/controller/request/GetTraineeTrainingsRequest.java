package org.example.trainee.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.core.validator.DateRangeProvider;
import org.example.core.validator.ValidDateRange;
import org.example.trainingType.dto.TrainingType;

import java.time.LocalDate;

@ValidDateRange
public record GetTraineeTrainingsRequest(
        @NotBlank(message = "Username cannot be blank")
        String username,
        LocalDate fromDate,
        LocalDate toDate,

        @Size(max = 50, message = "Trainer name cannot exceed 50 characters")
        String trainerName,
        TrainingType trainingType
) implements DateRangeProvider{
}
