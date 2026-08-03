package org.example.training.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "DTO for displaying trainings summaries")
public record Trainings (
        @Schema(description = "List of trainings summaries")
        List<TrainingSummary> trainings
) {
}
