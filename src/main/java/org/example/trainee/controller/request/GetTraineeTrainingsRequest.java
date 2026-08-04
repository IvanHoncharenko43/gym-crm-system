package org.example.trainee.controller.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.trainingType.dto.TrainingType;

import java.time.LocalDate;

public record GetTraineeTrainingsRequest(
        @NotBlank(message = "Username cannot be blank")
        String username,
        LocalDate fromDate,
        LocalDate toDate,

        @Size(max = 50, message = "Trainer name cannot exceed 50 characters")
        String trainerName,
        TrainingType trainingType
) {
        @AssertTrue(message = "The 'from' date cannot be after the 'to' date")
        public boolean isDateRangeValid() {
                if (fromDate == null || toDate == null) {
                        return true;
                }
                return !fromDate.isAfter(toDate);
        }
}
