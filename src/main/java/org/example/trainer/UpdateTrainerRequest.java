package org.example.trainer;


import org.example.shared.FullName;
import org.example.shared.TrainingType;

public record UpdateTrainerRequest(
        Long id,
        FullName fullName,
        TrainingType specialization,
        boolean isActive
) {
}
