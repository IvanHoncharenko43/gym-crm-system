package org.example.core;

import org.example.core.service.AuthenticationComponent;
import org.example.exception.AuthenticationFailedException;
import org.example.user.dto.UserCredentials;
import org.example.user.repository.UserEntity;
import org.example.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class AuthenticationComponentTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticationComponent authComponent;

    private static final String USERNAME = "User";
    private static final String PASSWORD = "Password1234";
    private static final UserCredentials CREDENTIALS = new UserCredentials(USERNAME, PASSWORD);

    @Test
    void authenticate_ApproveAuthentication_CredentialsAreValid(){
        UserEntity user = new UserEntity();
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        authComponent.authenticate(CREDENTIALS);
        verify(userRepository, times(1)).findByUsername(USERNAME);
    }

    @Test
    void authenticate_ThrowAuthenticationFailedException_UserDoesNotExist(){
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        AuthenticationFailedException exception = assertThrows(AuthenticationFailedException.class,
                () -> authComponent.authenticate(CREDENTIALS));
        assertTrue(exception.getMessage().contains("Invalid username"));
    }

    @Test
    void authenticate_ThrowAuthenticationFailedException_PasswordDoesNotMatch(){
        UserEntity user = new UserEntity();
        user.setUsername(USERNAME);
        user.setPassword("differentPassword");

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        AuthenticationFailedException exception = assertThrows(AuthenticationFailedException.class,
                () -> authComponent.authenticate(CREDENTIALS));
        assertTrue(exception.getMessage().contains("Invalid password"));
    }
}
