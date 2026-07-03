package org.example.trainee;

import org.example.shared.FullName;

import java.time.LocalDate;

public record UpdateTraineeRequest(
        Long id,
        FullName fullName,
        LocalDate dateOfBirth,
        String address,
        boolean isActive
) {
}
