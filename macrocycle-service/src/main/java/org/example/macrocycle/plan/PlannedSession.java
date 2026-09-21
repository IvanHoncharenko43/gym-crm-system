package org.example.macrocycle.plan;

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
