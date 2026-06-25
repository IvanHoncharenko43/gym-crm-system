package org.example.dto;

public record UserProfile (
        String firstName,
        String lastName,
        String username,
        String password,
        boolean isActive
){
}
