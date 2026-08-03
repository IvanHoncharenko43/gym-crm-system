package org.example.trainer.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.trainingType.dto.TrainingType;
import org.example.user.controller.dto.UserProfile;

@Schema(description = "DTO for displaying trainer's profile information")
public record TrainerSummary(
        @Schema(description = "ID of the trainer", example = "12")
        Long id,

        @Schema(description = "Details of the trainer's profile")
        UserProfile profile,

        @Schema(description = "Specialization of the trainer", example = "YOGA", implementation = TrainingType.class)
        TrainingType specialization
) {
}
