package org.example.macrocycle.plan;

import java.util.List;

public record VerifierVerdict(
        boolean approved,
        List<VerifierIssue> issues
) {
}
