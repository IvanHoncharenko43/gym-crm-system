package org.example.core.service;

import org.example.exception.AuthenticationFailedException;
import org.example.user.controller.dto.UserCredentials;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;


@Component
public class BasicAuthDecoder {

    public UserCredentials decode(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            throw new AuthenticationFailedException("Missing or invalid Authorization header");
        }
        String base64Credentials = authHeader.substring(6);
        byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
        String credentials = new String(decodedBytes, StandardCharsets.UTF_8);
        String[] values = credentials.split(":", 2);
        if (values.length != 2) {
            throw new AuthenticationFailedException("Invalid Basic authentication token");
        }
        return new UserCredentials(values[0], values[1]);
    }
}
