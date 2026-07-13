package org.example.training.dto;


import org.example.user.dto.UserCredentials;

import java.time.LocalDate;

public record CreateTrainingRequest(
        UserCredentials credentials,
        Long trainerId,
        Long traineeId,
        String trainingName,
        LocalDate trainingDate,
        int durationMinutes
) {
}
