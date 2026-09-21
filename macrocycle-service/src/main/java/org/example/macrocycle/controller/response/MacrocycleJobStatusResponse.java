package org.example.macrocycle.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.macrocycle.plan.JobStatus;
import org.example.macrocycle.plan.MacrocyclePlan;

import java.util.Set;

@Schema(description = "Status and, once ready, the generated plan for a macrocycle job", name = "MacrocycleJobStatus")
public record MacrocycleJobStatusResponse(
        String jobId,
        String traineeUsername,
        String supervisingTrainerUsername,
        JobStatus status,
        MacrocyclePlan plan,
        Set<String> warnings,
        String failureReason
) {
}
