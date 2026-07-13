package org.example.trainee.dto;

import org.example.training.dto.TrainingTypeSummary;
import org.example.user.dto.UserCredentials;

import java.time.LocalDate;

public record GetTraineeTrainingsRequest(
        UserCredentials credentials,
        LocalDate fromDate,
        LocalDate toDate,
        String trainerName,
        TrainingTypeSummary trainingType
) {
}
