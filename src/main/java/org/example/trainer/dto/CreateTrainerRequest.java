package org.example.trainer.dto;


import org.example.training.dto.TrainingType;
import org.example.user.dto.FullName;

public record CreateTrainerRequest(
        FullName fullName,
        TrainingType specialization
) {
}
