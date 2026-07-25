package org.example.trainee.dto;

import jakarta.validation.constraints.Size;
import org.example.core.validator.DateRangeProvider;
import org.example.core.validator.ValidDateRange;
import org.example.training.dto.TrainingType;
import org.example.user.dto.UserCredentials;

import java.time.LocalDate;

@ValidDateRange
public record GetTraineeTrainingsRequest(
        LocalDate fromDate,
        LocalDate toDate,

        @Size(max = 50)
        String trainerName,
        TrainingType trainingType
) implements DateRangeProvider{
}
