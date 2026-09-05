package org.example.crm.training.event;

import java.time.LocalDate;

public record TrainingChangedEvent(
        String trainerUsername,
        LocalDate trainingDate
) {
}
