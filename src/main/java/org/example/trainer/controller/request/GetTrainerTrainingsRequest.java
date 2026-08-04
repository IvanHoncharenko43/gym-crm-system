package org.example.trainer.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "DTO for getting trainer's training", name = "GetTrainerTrainings")
public record GetTrainerTrainingsRequest(
        @NotBlank(message = "Username cannot be blank")
        String username,
        LocalDate fromDate,
        LocalDate toDate,

        @Size(max = 50, message = "Trainee name cannot exceed 50 characters")
        String traineeName
) {
        @Schema(hidden = true)
        @AssertTrue(message = "The 'from' date cannot be after the 'to' date")
        public boolean isDateRangeValid() {
                if (fromDate == null || toDate == null) {
                        return true;
                }
                return !fromDate.isAfter(toDate);
        }
}
