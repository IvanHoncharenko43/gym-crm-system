package org.example.trainee.dto;

import org.example.user.dto.Credentials;
import org.example.user.dto.FullName;

import java.time.LocalDate;

public record UpdateTraineeRequest(
        Long id,
        Credentials credentials,
        FullName fullName,
        LocalDate dateOfBirth,
        String address
) {
}
