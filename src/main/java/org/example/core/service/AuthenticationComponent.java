package org.example.core.service;

import org.example.exception.AuthenticationFailedException;
import org.example.user.dto.UserCredentials;
import org.example.user.repository.UserEntity;
import org.example.user.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationComponent {

    private final UserRepository userRepository;

    public AuthenticationComponent(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public void authenticate(UserCredentials credentials) {
        UserEntity user = userRepository.findByUsername(credentials.username())
                .orElseThrow(() -> new AuthenticationFailedException("Invalid username for authentication"));
        if (!user.getPassword().equals(credentials.password())) {
            throw new AuthenticationFailedException("Invalid password for authentication");
        }
    }
}
