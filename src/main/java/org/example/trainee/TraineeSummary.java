package org.example.trainee;

import java.time.LocalDate;

public record TraineeSummary(
        Long id,
        String firstName,
        String lastName,
        String username,
        LocalDate dateOfBirth,
        String address
) {
}
