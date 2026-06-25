package org.example.shared;

public record UserProfile (
        String firstName,
        String lastName,
        String username,
        String password,
        boolean isActive
){
}
