package org.example.trainer.controller.response;

import org.example.trainingType.dto.TrainingType;
import org.example.user.controller.dto.UserProfile;

public record TrainerSummary(
        Long id,
        UserProfile profile,
        TrainingType specialization
) {
}
