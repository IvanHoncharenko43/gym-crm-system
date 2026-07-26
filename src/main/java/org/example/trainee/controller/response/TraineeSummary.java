package org.example.trainee.controller.response;

import org.example.user.controller.dto.UserProfile;

import java.time.LocalDate;

public record TraineeSummary(
        Long id,
        UserProfile profile,
        LocalDate dateOfBirth,
        String address
) {
}
