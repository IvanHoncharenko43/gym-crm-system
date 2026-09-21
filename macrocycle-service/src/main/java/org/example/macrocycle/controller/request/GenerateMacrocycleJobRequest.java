package org.example.macrocycle.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.macrocycle.plan.MacrocycleGoal;
import org.example.macrocycle.plan.TraineeContextSnapshot;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

@Schema(description = "Internal request from crm-service to start a macrocycle generation job", name = "GenerateMacrocycleJob")
public record GenerateMacrocycleJobRequest(

        @NotBlank
        String traineeUsername,

        @NotBlank
        String supervisingTrainerUsername,

        @NotNull
        MacrocycleGoal goal,

        @NotNull
        LocalDate targetEventDate,

        @Size(max = 500)
        String injuriesAndConstraints,

        @NotEmpty
        Set<DayOfWeek> weeklyAvailability,

        int preferredSessionDurationMinutes,

        @NotNull
        TraineeContextSnapshot contextSnapshot
) {
}
