package org.example.trainer;


import org.example.shared.TrainingType;

public record UpdateTrainerRequest(
        Long id,
        String firstName,
        String lastName,
        TrainingType specialization,
        boolean isActive
) {
}
