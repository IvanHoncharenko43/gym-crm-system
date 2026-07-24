package org.example.core.dto;

import org.example.user.dto.UserCredentials;

public record ChangeActivityRequest(
        UserCredentials credentials
) {
}
