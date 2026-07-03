package org.example.trainee;


import org.example.shared.FullName;

import java.time.LocalDate;

public record CreateTraineeRequest(
        FullName fullName,
        LocalDate dateOfBirth,
        String address
) {
}
