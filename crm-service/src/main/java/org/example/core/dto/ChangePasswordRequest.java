package org.example.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.example.core.validator.ValidPassword;

@Schema(description = "DTO for changing a password of the user", name = "ChangePassword")
public record ChangePasswordRequest(

        @Schema(description = "Old password of the user", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Old password cannot be blank")
        String oldPassword,

        @Schema(description = "New password to be set to the user", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "New password cannot be blank")
        @ValidPassword
        String newPassword
) {
}
