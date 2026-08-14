package org.example.core;

import org.example.core.service.AuthenticationComponent;
import org.example.exception.AuthenticationFailedException;
import org.example.user.controller.dto.UserCredentials;
import org.example.user.repository.UserEntity;
import org.example.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Optional;

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
        user.setIsActive(true);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        authComponent.authenticate(CREDENTIALS);
        verify(userRepository, times(1)).findByUsername(USERNAME);
    }

    @Test
    void authenticate_ThrowAuthenticationFailedException_UserDoesNotExist(){
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(AuthenticationFailedException.class,
                () -> authComponent.authenticate(CREDENTIALS));
    }

    @Test
    void authenticate_ThrowAuthenticationFailedException_PasswordDoesNotMatch(){
        UserEntity user = new UserEntity();
        user.setUsername(USERNAME);
        user.setPassword("differentPassword");
        user.setIsActive(true);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        assertThrows(AuthenticationFailedException.class,
                () -> authComponent.authenticate(CREDENTIALS));
    }
}
