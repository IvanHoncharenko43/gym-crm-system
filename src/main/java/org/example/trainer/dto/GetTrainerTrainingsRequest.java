package org.example.trainer.dto;

import org.example.user.dto.UserCredentials;

import java.time.LocalDate;

public record GetTrainerTrainingsRequest(
        UserCredentials credentials,
        LocalDate fromDate,
        LocalDate toDate,
        String traineeName
) {
}
