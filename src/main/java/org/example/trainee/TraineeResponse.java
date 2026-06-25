package org.example.trainee;

import org.example.dto.UserProfile;

import java.time.LocalDate;

public record TraineeResponse(
        Long id,
        UserProfile profile,
        LocalDate dateOfBirth,
        String address
) {
}
