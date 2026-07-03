package org.example.training;


import java.time.LocalDate;

public record CreateTrainingRequest(
        Long trainerId,
        Long traineeId,
        String trainingName,
        LocalDate trainingDate,
        int durationMinutes
) {
}
