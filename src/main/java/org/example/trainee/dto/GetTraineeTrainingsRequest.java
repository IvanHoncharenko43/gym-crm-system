package org.example.trainee.dto;

import org.example.training.dto.TrainingTypeSummary;
import org.example.user.dto.Credentials;

import java.time.LocalDate;

public record GetTraineeTrainingsRequest(
        Credentials credentials,
        LocalDate fromDate,
        LocalDate toDate,
        String trainerName,
        TrainingTypeSummary trainingType
) {
}
