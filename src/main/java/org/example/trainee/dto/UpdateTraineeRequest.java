package org.example.trainee.dto;

import org.example.user.dto.UserCredentials;
import org.example.user.dto.FullName;

import java.time.LocalDate;

public record UpdateTraineeRequest(
        Long id,
        UserCredentials credentials,
        FullName fullName,
        LocalDate dateOfBirth,
        String address
) {
}
