package org.example.trainer.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "DTO for displaying trainers' profiles information")
public record Trainers(
        @Schema(description = "List of profiles information about the trainers")
        List<TrainerSummary> trainers
) {
}
