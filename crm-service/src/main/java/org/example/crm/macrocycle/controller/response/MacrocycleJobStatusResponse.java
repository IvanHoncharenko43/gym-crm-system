package org.example.crm.macrocycle.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.crm.macrocycle.plan.JobStatus;
import org.example.crm.macrocycle.plan.MacrocyclePlan;

import java.util.Set;

@Schema(description = "Status and, once ready, the generated plan for a macrocycle job", name = "MacrocycleJobStatus")
public record MacrocycleJobStatusResponse(
        String jobId,
        JobStatus status,
        MacrocyclePlan plan,
        Set<String> warnings,
        String failureReason
) {
}
