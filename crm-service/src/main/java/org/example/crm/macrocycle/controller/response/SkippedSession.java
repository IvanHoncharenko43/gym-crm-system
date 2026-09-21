package org.example.crm.macrocycle.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.DayOfWeek;

@Schema(description = "A planned session that could not be materialized into a training", name = "SkippedSession")
public record SkippedSession(
        int weekNumber,
        DayOfWeek dayOfWeek,
        String reason
) {
}
