package org.example.trainer.dto;

import java.time.LocalDate;

public record GetTrainerTrainingsRequest(
        String username,
        LocalDate fromDate,
        LocalDate toDate,
        String traineeName
) {
}
