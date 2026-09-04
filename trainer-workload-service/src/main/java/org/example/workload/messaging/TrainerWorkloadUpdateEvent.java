package org.example.workload.messaging;

import org.example.workload.controller.dto.FullName;

import java.time.LocalDate;

public record TrainerWorkloadUpdateEvent(
        String username,
        FullName fullName,
        boolean isActive,
        LocalDate trainingDate,
        int trainingSummaryDurationMinutes
) {
}
