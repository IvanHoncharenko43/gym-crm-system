package org.example.user;

import org.example.core.dto.ChangePasswordRequest;
import org.example.core.service.AuthenticationComponent;
import org.example.exception.EntityNotFoundException;
import org.example.user.dto.UserCredentials;
import org.example.user.repository.UserEntity;
import org.example.user.repository.UserRepository;
import org.example.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    private static final String USERNAME = "John.Doe";
    private static final String PASSWORD = "122333test";
    private static final UserCredentials CREDENTIALS = new UserCredentials(USERNAME, PASSWORD);

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationComponent authenticator;

    @InjectMocks
    private UserService userService;

    @Test
    void changePassword_Change_RequestIsValid(){
        String newPassword = "New_Password";
        ChangePasswordRequest request = new ChangePasswordRequest(USERNAME, PASSWORD, newPassword);
        UserEntity existingUser = new UserEntity();
        existingUser.setPassword(PASSWORD);
        existingUser.setIsActive(true);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(existingUser));

        userService.changePassword(request, CREDENTIALS);

        verify(authenticator, times(1)).authenticate(CREDENTIALS);
        verify(authenticator, times(1)).authorize(USERNAME, CREDENTIALS);
        verify(userRepository, times(1)).findByUsername(USERNAME);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void changePassword_ThrowEntityNotFoundException_UserDoesNotExist(){
        String newPassword = "New_Password";
        ChangePasswordRequest request = new ChangePasswordRequest(USERNAME, PASSWORD, newPassword);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.changePassword(request, CREDENTIALS));
        assertTrue(exception.getMessage().contains("not found"));
        verify(authenticator, times(1)).authenticate(CREDENTIALS);
        verify(authenticator, times(1)).authorize(USERNAME, CREDENTIALS);
        verify(userRepository, times(1)).findByUsername(USERNAME);
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_ThrowEntityNotFoundException_UserIsInactive() {
        String newPassword = "New_Password";
        ChangePasswordRequest request = new ChangePasswordRequest(USERNAME, PASSWORD, newPassword);
        UserEntity inactiveUser = new UserEntity();
        inactiveUser.setIsActive(false);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(inactiveUser));
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.changePassword(request, CREDENTIALS));
        assertTrue(exception.getMessage().contains("inactive"));
        verify(authenticator, times(1)).authenticate(CREDENTIALS);
        verify(authenticator, times(1)).authorize(USERNAME, CREDENTIALS);
        verify(userRepository, times(1)).findByUsername(USERNAME);
        verify(userRepository, never()).save(any());
    }
}
