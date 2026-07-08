package org.example.core.dto;

import org.example.user.dto.Credentials;

public record ChangePasswordRequest(
        Credentials credentials,
        String newPassword
) {
}
