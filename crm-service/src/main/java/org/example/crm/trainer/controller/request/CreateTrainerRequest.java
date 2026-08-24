package org.example.crm.trainer.controller.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.example.crm.trainingType.dto.TrainingType;
import org.example.crm.user.controller.dto.FullName;

@Schema(description = "DTO for registering a new trainer", name = "CreateTrainer")
public record CreateTrainerRequest(

        @Schema(description = "The first and last names of the trainer", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Full name cannot be null")
        @Valid
        FullName fullName,

        @Schema(description = "Specialization of the trainer", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Specialization cannot be null")
        TrainingType specialization
) {
}
