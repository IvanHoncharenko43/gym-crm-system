package org.example.trainer;

import org.example.shared.TrainingType;
import org.example.shared.UserProfile;

public record TrainerResponse(
        Long id,
        UserProfile userProfile,
        TrainingType specialization
) {
}
