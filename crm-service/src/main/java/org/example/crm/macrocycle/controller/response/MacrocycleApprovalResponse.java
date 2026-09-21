package org.example.crm.macrocycle.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Result of approving and materializing a macrocycle plan into real trainings", name = "MacrocycleApproval")
public record MacrocycleApprovalResponse(
        List<Long> createdTrainingIds,
        List<SkippedSession> skippedSessions
) {
}
