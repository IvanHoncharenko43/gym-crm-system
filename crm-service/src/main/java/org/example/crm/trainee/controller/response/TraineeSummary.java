package org.example.crm.trainee.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.crm.user.controller.dto.UserProfile;

import java.time.LocalDate;

@Schema(description = "DTO for displaying trainee's profile information")
public record TraineeSummary(
        @Schema(description = "ID of the trainee", example = "12")
        Long id,

        @Schema(description = "Details of the trainee's profile")
        UserProfile profile,

        @Schema(description = "Date of birth of the trainee (YYYY-MM-DD)", example = "2007-01-01")
        LocalDate dateOfBirth,

        @Schema(description = "Address of the trainee", example = "Town, Home Street, 21")
        String address
) {
}
