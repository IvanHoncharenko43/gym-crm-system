package org.example.crm.trainer.client.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.crm.user.controller.dto.FullName;

@Schema(description = "DTO for a trainer's monthly training summary", name = "TrainerWorkloadSummaryResponse")
public record TrainerWorkloadSummaryResponse(
        @Schema(description = "Username of the trainer")
        String username,

        @Schema(description = "The first and last names of the trainer")
        FullName fullName,

        @Schema(description = "Activity status of the trainer")
        boolean isActive,

        @Schema(description = "Year of the requested summary")
        int year,

        @Schema(description = "Month of the requested summary (1-12)")
        int month,

        @Schema(description = "Training summary duration of the trainer in minutes")
        int trainingSummaryDurationMinutes
) {
}
