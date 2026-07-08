package org.example.core.dto;

import org.example.user.dto.Credentials;

public record ChangeActivityRequest(
        Credentials credentials,
        boolean isActive
) {
}
