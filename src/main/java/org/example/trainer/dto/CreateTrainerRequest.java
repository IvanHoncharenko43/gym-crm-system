package org.example.trainer.dto;


import org.example.user.dto.FullName;
import org.example.training.enums.TrainingType;

public record CreateTrainerRequest(
        FullName fullName,
        TrainingType specialization
) {
}
