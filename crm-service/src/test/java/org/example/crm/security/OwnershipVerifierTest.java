package org.example.crm.security;

import org.example.crm.TestUtils;
import org.example.crm.exception.AccessForbiddenException;
import org.example.crm.security.service.OwnershipVerifier;
import org.example.crm.trainee.repository.TraineeRepository;
import org.example.crm.trainer.repository.TrainerRepository;
import org.example.crm.user.controller.dto.UserRole;
import org.example.crm.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class OwnershipVerifierTest {
    private static final String TARGET_USERNAME = TestUtils.TRAINEE_USERNAME;
    private static final UserDetails USER_DETAILS = TestUtils.getTraineeUserDetails();

    @Mock
    private UserRepository userRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private OwnershipVerifier ownershipVerifier;

    @Test
    void verifyOwnershipByUsername_ApproveOwnership_UsernamesAreMatching(){
        ownershipVerifier.verifyOwnership(TARGET_USERNAME, USER_DETAILS);
    }

    @Test
    void verifyOwnershipByUsername_ThrowAccessForbiddenException_UsernamesAreNotMatching(){
        UserDetails userDetails = new User("John.Doe1", "password1234", true,
                true,true, true,
                List.of(new SimpleGrantedAuthority(UserRole.TRAINEE.getAuthority())));
        assertThrows(AccessForbiddenException.class,
                () -> ownershipVerifier.verifyOwnership(TARGET_USERNAME, userDetails));
    }
}
