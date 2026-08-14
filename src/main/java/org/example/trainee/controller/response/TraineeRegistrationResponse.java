package org.example.trainee.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.security.controller.dto.LoginDetails;

@Schema(description = "DTO containing information trainee registration details")
public record TraineeRegistrationResponse(
        TraineeSummary traineeSummary,
        LoginDetails loginDetails
) {
}
