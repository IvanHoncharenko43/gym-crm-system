package org.example.trainee.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.core.validator.ValidTraineeAge;
import org.example.user.controller.dto.FullName;

import java.time.LocalDate;

@Schema(description = "DTO for updating the existing trainee's profile", name = "UpdateTrainee")
public record UpdateTraineeRequest(

        @Schema(description = "The first and last names of the trainee", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Full name cannot be null")
        @Valid
        FullName fullName,

        @Schema(description = "Date of birth of the trainee", example = "2007-01-01")
        @ValidTraineeAge
        LocalDate dateOfBirth,

        @Schema(description = "The residential address of the trainee", example = "123 Home Address, City")
        @Size(max = 200, message = "Address is too long")
        String address
) {
}
