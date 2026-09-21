package org.example.crm.macrocycle.client.request;

import org.example.crm.macrocycle.plan.MacrocycleGoal;
import org.example.crm.macrocycle.plan.TraineeContextSnapshot;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

public record GenerateMacrocycleJobRequest(
        String traineeUsername,
        String supervisingTrainerUsername,
        MacrocycleGoal goal,
        LocalDate targetEventDate,
        String injuriesAndConstraints,
        Set<DayOfWeek> weeklyAvailability,
        int preferredSessionDurationMinutes,
        TraineeContextSnapshot contextSnapshot
) {
}
