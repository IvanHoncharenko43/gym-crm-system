package org.example.crm.macrocycle.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.crm.macrocycle.plan.MacrocycleGoal;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

@Schema(description = "DTO for requesting an AI-generated training macrocycle", name = "GenerateMacrocycle")
public record GenerateMacrocycleRequest(

        @Schema(description = "The trainee's training goal", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Goal cannot be null")
        MacrocycleGoal goal,

        @Schema(description = "Target event date the plan should build towards", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Target event date cannot be null")
        @ValidTargetEventDate
        LocalDate targetEventDate,

        @Schema(description = "Username of the trainee's assigned trainer who will supervise this macrocycle", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Supervising trainer username cannot be blank")
        String supervisingTrainerUsername,

        @Schema(description = "Free-text description of current injuries or scheduling constraints")
        @Size(max = 500, message = "Injuries and constraints must not exceed 500 characters")
        String injuriesAndConstraints,

        @Schema(description = "Days of the week the trainee is available to train", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "Weekly availability cannot be empty")
        Set<DayOfWeek> weeklyAvailability,

        @Schema(description = "Preferred session duration in minutes", requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(value = 20, message = "Preferred session duration must be at least 20 minutes")
        @Max(value = 180, message = "Preferred session duration cannot exceed 180 minutes")
        int preferredSessionDurationMinutes
) {
}
