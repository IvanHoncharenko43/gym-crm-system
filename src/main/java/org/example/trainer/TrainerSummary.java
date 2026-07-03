package org.example.trainer;

import org.example.shared.TrainingType;
import org.example.shared.UserProfile;

public record TrainerSummary(
        Long id,
        UserProfile profile,
        TrainingType specialization
) {
}
