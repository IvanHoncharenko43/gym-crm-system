package org.example.crm.macrocycle.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.crm.macrocycle.plan.JobStatus;

@Schema(description = "Confirmation that a macrocycle generation job was accepted", name = "MacrocycleJobAccepted")
public record MacrocycleJobAccepted(
        String jobId,
        JobStatus status
) {
}
