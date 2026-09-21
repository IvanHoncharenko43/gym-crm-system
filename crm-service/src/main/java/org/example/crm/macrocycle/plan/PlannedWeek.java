package org.example.crm.macrocycle.plan;

import java.util.List;

public record PlannedWeek(
        int weekNumber,
        String focus,
        List<PlannedSession> sessions
) {
}
