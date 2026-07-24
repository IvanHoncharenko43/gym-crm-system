package org.example.trainer.dto;


import org.example.training.dto.TrainingType;
import org.example.user.dto.UserCredentials;
import org.example.user.dto.FullName;

public record UpdateTrainerRequest(
        Long id,
        UserCredentials credentials,
        FullName fullName,
        TrainingType specialization
) {
}
