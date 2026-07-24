package org.example.trainee.dto;

import org.example.training.dto.TrainingType;
import org.example.user.dto.UserCredentials;

import java.time.LocalDate;

public record GetTraineeTrainingsRequest(
        UserCredentials credentials,
        LocalDate fromDate,
        LocalDate toDate,
        String trainerName,
        TrainingType trainingType
) {
}
