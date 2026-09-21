package org.example.macrocycle.plan;

import java.util.List;

public record TraineeContextSnapshot(
        String traineeUsername,
        List<TrainingHistoryItem> recentTrainings
) {
}
