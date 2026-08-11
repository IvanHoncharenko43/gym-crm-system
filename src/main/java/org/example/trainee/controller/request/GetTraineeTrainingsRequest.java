package org.example.trainee.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.trainingType.dto.TrainingType;

import java.time.LocalDate;

@Schema(description = "DTO for getting trainee's training", name = "GetTraineeTrainings")
public record GetTraineeTrainingsRequest(
        @Schema(description = "Username of the trainee", example = "John.Doe", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Username cannot be blank")
        String username,

        @Schema(description = "The start date of the search period (YYYY-MM-DD)", example = "2026-01-01")
        LocalDate fromDate,

        @Schema(description = "The end date of the search period (YYYY-MM-DD)", example = "2026-12-31")
        LocalDate toDate,

        @Schema(description = "The name of the trainer", example = "Smith")
        @Size(max = 50, message = "Trainer name cannot exceed 50 characters")
        String trainerName,

        @Schema(description = "The type of the training")
        TrainingType trainingType
) {
        @JsonIgnore
        @AssertTrue(message = "The 'from' date cannot be after the 'to' date")
        public boolean isDateRangeValid() {
                if (fromDate == null || toDate == null) {
                        return true;
                }
                return !fromDate.isAfter(toDate);
        }
}
