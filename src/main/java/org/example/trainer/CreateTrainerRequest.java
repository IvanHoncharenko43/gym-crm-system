package org.example.trainer;


import org.example.shared.TrainingType;

public record CreateTrainerRequest(
        String firstName,
        String lastName,
        TrainingType specialization
) {
}
