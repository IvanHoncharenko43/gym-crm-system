package org.example.trainer;

import org.example.dto.TrainingTypeDto;

public record TrainerSummary(
        Long id,
        String firstName,
        String lastName,
        String username,
        TrainingTypeDto specialization
) {
}
