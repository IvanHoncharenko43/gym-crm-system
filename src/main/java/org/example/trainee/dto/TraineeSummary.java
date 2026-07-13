package org.example.trainee.dto;

import org.example.user.dto.UserProfile;

import java.time.LocalDate;

public record TraineeSummary(
        Long id,
        UserProfile profile,
        LocalDate dateOfBirth,
        String address
) {
}
