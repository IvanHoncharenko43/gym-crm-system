package org.example.user.controller.dto;

public enum UserRole {
    TRAINEE,
    TRAINER;

    public String getAuthority(){
        return "ROLE_" + name();
    }
}
