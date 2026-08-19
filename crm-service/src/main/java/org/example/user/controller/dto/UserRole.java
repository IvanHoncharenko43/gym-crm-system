package org.example.user.controller.dto;

public enum UserRole {
    TRAINEE,
    TRAINER,
    ADMIN;

    public String getAuthority(){
        return "ROLE_" + name();
    }
}
