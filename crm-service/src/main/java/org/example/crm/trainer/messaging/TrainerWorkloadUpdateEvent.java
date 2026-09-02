package org.example.crm.trainer.messaging;

import org.example.crm.core.dto.ActionType;
import org.example.crm.user.controller.dto.FullName;

import java.time.LocalDate;

public record TrainerWorkloadUpdateEvent(
        String username,
        FullName fullName,
        boolean isActive,
        LocalDate trainingDate,
        int trainingSummaryDurationMinutes,
        ActionType actionType
) {
}
