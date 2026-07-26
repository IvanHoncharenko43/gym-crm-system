package org.example.training.dto;

import java.time.LocalDate;

public record CreateTrainingRequest(
        String trainerUsername,
        String traineeUsername,
        String trainingName,
        LocalDate trainingDate,
        int durationMinutes
) {
}
