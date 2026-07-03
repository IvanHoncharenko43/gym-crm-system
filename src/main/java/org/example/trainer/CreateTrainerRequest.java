package org.example.trainer;


import org.example.shared.FullName;
import org.example.shared.TrainingType;

public record CreateTrainerRequest(
        FullName fullName,
        TrainingType specialization
) {
}
