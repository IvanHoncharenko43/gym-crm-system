package org.example.trainee.dto;


import org.example.user.dto.FullName;

import java.time.LocalDate;

public record CreateTraineeRequest(
        FullName fullName,
        LocalDate dateOfBirth,
        String address
) {
}
