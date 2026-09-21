package org.example.crm.macrocycle.client.response;

import org.example.crm.macrocycle.plan.JobStatus;

public record MacrocycleJobAcceptedResponse(
        String jobId,
        JobStatus status
) {
}
