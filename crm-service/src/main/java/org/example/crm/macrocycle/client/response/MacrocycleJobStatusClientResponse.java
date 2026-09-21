package org.example.crm.macrocycle.client.response;

import org.example.crm.macrocycle.plan.JobStatus;
import org.example.crm.macrocycle.plan.MacrocyclePlan;

import java.util.Set;

public record MacrocycleJobStatusClientResponse(
        String jobId,
        String traineeUsername,
        String supervisingTrainerUsername,
        JobStatus status,
        MacrocyclePlan plan,
        Set<String> warnings,
        String failureReason
) {
}
