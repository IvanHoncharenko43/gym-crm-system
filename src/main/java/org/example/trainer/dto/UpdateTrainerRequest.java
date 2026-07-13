package org.example.trainer.dto;


import org.example.training.dto.TrainingTypeSummary;
import org.example.user.dto.UserCredentials;
import org.example.user.dto.FullName;

public record UpdateTrainerRequest(
        UserCredentials credentials,
        Long id,
        FullName fullName,
        TrainingTypeSummary specialization
) {
}
