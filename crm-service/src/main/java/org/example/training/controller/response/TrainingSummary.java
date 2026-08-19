package org.example.training.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.trainee.controller.response.TraineeSummary;
import org.example.trainer.controller.response.TrainerSummary;
import org.example.trainingType.dto.TrainingType;

import java.time.LocalDate;

@Schema(description = "DTO for displaying training summary information")
public record TrainingSummary(
        @Schema(description = "ID of the training", example = "12")
        Long id,

        @Schema(description = "DTO for displaying trainer's profile information")
        TrainerSummary trainer,

        @Schema(description = "DTO for displaying trainee's profile information")
        TraineeSummary trainee,

        @Schema(description = "The name of the training", example = "Morning Cardio")
        String trainingName,

        @Schema(description = "The type of the training", example = "YOGA", implementation = TrainingType.class)
        TrainingType trainingType,

        @Schema(description = "The date of the training (YYYY-MM-DD)", example = "2026-08-09")
        LocalDate trainingDate,

        @Schema(description = "Duration of the training in minutes")
        int durationMinutes
) {
}
