package org.example.trainee.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import org.example.core.validator.ValidTraineeAge;
import org.example.user.dto.FullName;

import java.time.LocalDate;

public record UpdateTraineeRequest(
        @NotNull(message = "Full name cannot be null")
        @Valid
        FullName fullName,

        @Past(message = "Date of birth must be in the past")
        @ValidTraineeAge
        LocalDate dateOfBirth,

        @Size(max = 200, message = "Address is too long")
        String address
) {
}
