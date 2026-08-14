package org.example.user;

import org.example.TestUtils;
import org.example.core.dto.ChangePasswordRequest;
import org.example.security.service.OwnershipVerifier;
import org.example.exception.EntityNotFoundException;
import org.example.user.repository.UserEntity;
import org.example.user.repository.UserRepository;
import org.example.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

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
    private static final UserDetails USER_DETAILS = TestUtils.getTraineeUserDetails();

    @Mock
    private UserRepository userRepository;

    @Mock
    private OwnershipVerifier ownershipVerifier;

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

        userService.changePassword(userId, request, USER_DETAILS);

        verify(ownershipVerifier, times(1)).verifyOwnershipByUsername(USERNAME, USER_DETAILS.getUsername());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void changePassword_ThrowEntityNotFoundException_UserDoesNotExist(){
        String newPassword = "New_Password";
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest(PASSWORD, newPassword);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.changePassword(userId, request, USER_DETAILS));
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
                () -> userService.changePassword(userId, request, USER_DETAILS));
        assertTrue(exception.getMessage().contains("inactive"));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
    }
}
