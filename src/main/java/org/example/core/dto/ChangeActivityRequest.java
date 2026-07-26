package org.example.core.dto;

import org.example.user.controller.dto.UserCredentials;

public record ChangeActivityRequest(
        UserCredentials credentials
) {
}
