package org.example.crm.macrocycle.plan;

import org.example.crm.trainingType.dto.TrainingType;

import java.time.DayOfWeek;

public record PlannedSession(
        int weekNumber,
        DayOfWeek dayOfWeek,
        TrainingType trainingType,
        String title,
        int durationMinutes,
        String coachingNotes
) {
}
