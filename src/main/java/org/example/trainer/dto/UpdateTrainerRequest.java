package org.example.trainer.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.example.training.dto.TrainingType;
import org.example.user.dto.FullName;

public record UpdateTrainerRequest(
        @NotNull(message = "Full name cannot be null")
        @Valid
        FullName fullName,
        TrainingType specialization
) {
}
