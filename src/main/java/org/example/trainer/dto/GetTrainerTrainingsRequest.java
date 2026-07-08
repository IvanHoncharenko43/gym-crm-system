package org.example.trainer.dto;

import org.example.user.dto.Credentials;

import java.time.LocalDate;

public record GetTrainerTrainingsRequest(
        Credentials credentials,
        LocalDate fromDate,
        LocalDate toDate,
        String traineeName
) {
}
