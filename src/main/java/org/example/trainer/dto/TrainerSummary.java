package org.example.trainer.dto;

import org.example.training.dto.TrainingType;
import org.example.user.dto.UserProfile;

public record TrainerSummary(
        Long id,
        UserProfile profile,
        TrainingType specialization
) {
}
