package org.example.trainer.controller.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.example.trainingType.dto.TrainingType;
import org.example.user.controller.dto.FullName;

public record UpdateTrainerRequest(
        @NotNull(message = "Full name cannot be null")
        @Valid
        FullName fullName,

        @NotNull(message = "Specialization cannot be null")
        TrainingType specialization
) {
}
