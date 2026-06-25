package org.example.trainer;

import org.example.domain.TrainingType;
import org.example.dto.UserProfile;

public record TrainerResponse(
        Long id,
        UserProfile userProfile,
        TrainingType specialization
) {
}
