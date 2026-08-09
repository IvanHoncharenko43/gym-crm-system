package org.example.trainee.controller.request;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import org.example.trainingType.dto.TrainingType;

import java.time.LocalDate;

@Getter
@Setter
public class GetTraineeTrainingsRequest{

        @Parameter(description = "Username of the trainee", example = "John.Doe")
        @QueryParam("username")
        @NotBlank(message = "Username cannot be blank")
        String username;

        @Parameter(description = "The start date of the search period (YYYY-MM-DD)", example = "2026-01-01")
        @QueryParam("fromDate")
        LocalDate fromDate;

        @Parameter(description = "The end date of the search period (YYYY-MM-DD)", example = "2026-12-31")
        @QueryParam("toDate")
        LocalDate toDate;

        @Parameter(description = "The name of the trainer", example = "Smith")
        @QueryParam("trainerName")
        @Size(max = 50, message = "Trainer name cannot exceed 50 characters")
        String trainerName;

        @Parameter(description = "The type of the training")
        @QueryParam("trainingType")
        TrainingType trainingType;

        @Parameter(hidden = true)
        @AssertTrue(message = "The 'from' date cannot be after the 'to' date")
        public boolean isDateRangeValid() {
                if (fromDate == null || toDate == null) {
                        return true;
                }
                return !fromDate.isAfter(toDate);
        }
}
