package org.example.crm.user;

import org.example.crm.core.dto.ChangePasswordRequest;
import org.example.crm.exception.EntityNotFoundException;
import org.example.crm.user.repository.UserEntity;
import org.example.crm.user.repository.UserRepository;
import org.example.crm.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void changePassword_Change_RequestIsValid(){
        String newPassword = "New_Password";
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest(PASSWORD, newPassword);
        UserEntity existingUser = new UserEntity();
        existingUser.setPassword(PASSWORD);
        existingUser.setIsActive(true);
        existingUser.setUsername(USERNAME);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(PASSWORD, PASSWORD)).thenReturn(true);

        userService.changePassword(userId, request);

        verify(userRepository, times(1)).findById(userId);
        verify(passwordEncoder, times(1)).matches(PASSWORD, PASSWORD);
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void changePassword_ThrowEntityNotFoundException_UserDoesNotExist(){
        String newPassword = "New_Password";
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest(PASSWORD, newPassword);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.changePassword(userId, request));
        assertTrue(exception.getMessage().contains("not found"));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_ThrowEntityNotFoundException_UserIsInactive() {
        String newPassword = "New_Password";
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest(PASSWORD, newPassword);
        UserEntity inactiveUser = new UserEntity();
        inactiveUser.setIsActive(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(inactiveUser));
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.changePassword(userId, request));
        assertTrue(exception.getMessage().contains("inactive"));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
    }
}
