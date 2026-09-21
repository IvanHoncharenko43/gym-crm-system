package org.example.macrocycle.plan;

public record VerifierIssue(
        String severity,
        String description,
        Integer weekNumber
) {
}
