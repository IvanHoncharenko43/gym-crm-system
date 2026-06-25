package org.example.trainer;


import org.example.domain.TrainingType;

public record CreateTrainerRequest(
        String firstName,
        String lastName,
        TrainingType specialization
) {
}
