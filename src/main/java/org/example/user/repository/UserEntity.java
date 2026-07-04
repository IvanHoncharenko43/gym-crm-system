package org.example.user.repository;

import lombok.Data;

@Data
public class UserEntity {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private boolean isActive;
}
