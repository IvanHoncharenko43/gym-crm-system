package org.example.training;


import java.time.LocalDate;

public record CreateTrainingRequest(
        String trainerUsername,
        String traineeUsername,
        String trainingName,
        LocalDate trainingDate,
        int duration
) {
}
