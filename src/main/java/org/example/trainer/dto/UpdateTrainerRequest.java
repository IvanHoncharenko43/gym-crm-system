package org.example.trainer.dto;


import org.example.training.dto.TrainingTypeSummary;
import org.example.user.dto.FullName;

public record UpdateTrainerRequest(
        Long id,
        FullName fullName,
        TrainingTypeSummary specialization,
        boolean isActive
) {
}
