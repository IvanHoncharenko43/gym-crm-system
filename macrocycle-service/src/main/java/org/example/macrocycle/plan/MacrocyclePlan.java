package org.example.macrocycle.plan;

import java.time.LocalDate;
import java.util.List;

public record MacrocyclePlan(
        MacrocycleGoal goal,
        LocalDate targetEventDate,
        List<PlannedWeek> weeks,
        List<String> warnings
) {
}
