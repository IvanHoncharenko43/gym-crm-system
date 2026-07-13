package org.example.core.dto;

import org.example.user.dto.UserCredentials;

public record ChangePasswordRequest(
        UserCredentials credentials,
        String newPassword
) {
}
