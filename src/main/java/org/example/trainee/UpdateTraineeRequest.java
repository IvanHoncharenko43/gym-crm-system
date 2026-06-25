package org.example.trainee;

import java.time.LocalDate;

public record UpdateTraineeRequest(
        Long id,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address,
        boolean isActive
) {
}
