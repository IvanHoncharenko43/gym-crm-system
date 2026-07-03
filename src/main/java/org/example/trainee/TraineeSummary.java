package org.example.trainee;

import org.example.shared.UserProfile;

import java.time.LocalDate;

public record TraineeSummary(
        Long id,
        UserProfile profile,
        LocalDate dateOfBirth,
        String address
) {
}
