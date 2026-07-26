package org.example.trainer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.core.validator.DateRangeProvider;
import org.example.core.validator.ValidDateRange;

import java.time.LocalDate;

@ValidDateRange
public record GetTrainerTrainingsRequest(
        @NotBlank(message = "Username cannot be blank")
        String username,

        LocalDate fromDate,
        LocalDate toDate,

        @Size(max = 50, message = "Trainee name cannot exceed 50 characters")
        String traineeName
) implements DateRangeProvider {
}
