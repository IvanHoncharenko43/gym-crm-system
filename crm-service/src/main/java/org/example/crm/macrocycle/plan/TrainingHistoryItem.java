package org.example.crm.macrocycle.plan;

import java.time.LocalDate;

public record TrainingHistoryItem(
        LocalDate date,
        String trainingTypeName,
        int durationMinutes,
        String trainingName
) {
}
