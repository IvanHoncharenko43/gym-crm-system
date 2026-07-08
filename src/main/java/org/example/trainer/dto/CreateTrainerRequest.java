package org.example.trainer.dto;


import org.example.training.dto.TrainingTypeSummary;
import org.example.user.dto.FullName;

public record CreateTrainerRequest(
        FullName fullName,
        TrainingTypeSummary specialization
) {
}
