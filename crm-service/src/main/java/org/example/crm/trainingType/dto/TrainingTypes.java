package org.example.crm.trainingType.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "DTO for displaying training types")
public record TrainingTypes(
        @Schema(description = "List of training types")
        List<TrainingType> trainingTypes
) {
}
