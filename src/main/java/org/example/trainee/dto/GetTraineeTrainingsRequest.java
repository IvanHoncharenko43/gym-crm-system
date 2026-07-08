package org.example.trainee.dto;

import org.example.training.enums.TrainingType;
import org.example.user.dto.Credentials;

import java.time.LocalDate;

public record GetTraineeTrainingsRequest(
        Credentials credentials,
        LocalDate fromDate,
        LocalDate toDate,
        String trainerName,
        TrainingType trainingType
) {
}
