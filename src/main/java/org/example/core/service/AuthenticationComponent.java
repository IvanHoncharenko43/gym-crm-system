package org.example.core.service;

import org.example.exception.AuthenticationFailedException;
import org.example.user.dto.UserCredentials;
import org.example.user.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationComponent {

    private final UserRepository userRepository;

    public AuthenticationComponent(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public void authenticate(UserCredentials credentials) {
        userRepository.findByUsername(credentials.username())
                .filter(u -> u.getPassword().equals(credentials.password()))
                .orElseThrow(() -> new AuthenticationFailedException("Authentication failed"));
    }
}
