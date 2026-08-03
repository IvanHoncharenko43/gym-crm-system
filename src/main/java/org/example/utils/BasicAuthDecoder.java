package org.example.utils;

import org.example.exception.AuthenticationFailedException;
import org.example.user.controller.dto.UserCredentials;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class BasicAuthDecoder {

    private static final int EXPECTED_CREDENTIALS_PARTS = 2;
    private static final String BASIC_PREFIX = "Basic ";
    private static final String CREDENTIALS_DELIMITER = ":";

    public UserCredentials decode(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BASIC_PREFIX)) {
            throw new AuthenticationFailedException("Missing or invalid Authorization header");
        }
        // basic auth has the format "basic encoded-credentials"
        String base64Credentials = authHeader.substring(6); // extracting out "basic "
        byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials); // decoding credentials
        String credentials = new String(decodedBytes, StandardCharsets.UTF_8);
        // decoded credentials are in the following format "username:password"
        String[] values = credentials.split(CREDENTIALS_DELIMITER, EXPECTED_CREDENTIALS_PARTS);
        if (values.length != EXPECTED_CREDENTIALS_PARTS) {
            throw new AuthenticationFailedException("Invalid Basic authentication token");
        }
        return new UserCredentials(values[0], values[1]);
    }
}
