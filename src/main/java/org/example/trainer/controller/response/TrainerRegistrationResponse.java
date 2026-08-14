package org.example.trainer.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.security.controller.dto.LoginDetails;

@Schema(description = "DTO containing information trainee registration details")
public record TrainerRegistrationResponse(
        TrainerSummary trainerSummary,
        LoginDetails loginDetails
) {
}
