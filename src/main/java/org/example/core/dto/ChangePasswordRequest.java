package org.example.core.dto;

import jakarta.validation.constraints.NotBlank;
import org.example.core.validator.ValidPassword;

public record ChangePasswordRequest(
        @NotBlank(message = "Username cannot be blank")
        String username,

        @NotBlank(message = "Old password cannot be blank")
        String oldPassword,

        @NotBlank(message = "New password cannot be blank")
        @ValidPassword
        String newPassword
) {
}
