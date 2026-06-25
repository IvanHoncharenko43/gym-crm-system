package org.example.trainer;


import org.example.shared.TrainingType;

public record TrainerSummary(
        Long id,
        String firstName,
        String lastName,
        String username,
        TrainingType specialization
) {
}
