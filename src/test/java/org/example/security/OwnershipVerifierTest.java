package org.example.security;

import org.example.exception.AccessForbiddenException;
import org.example.security.service.OwnershipVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class OwnershipVerifierTest {

    private OwnershipVerifier ownershipVerifier;

    private static final String TARGET_USERNAME = "John.Doe";
    private static final String CURRENT_USERNAME = TARGET_USERNAME;

    @BeforeEach
    void setUp(){
        this.ownershipVerifier = new OwnershipVerifier();
    }

    @Test
    void verifyOwnershipByUsername_ApproveOwnership_UsernamesAreMatching(){
        ownershipVerifier.verifyOwnershipByUsername(TARGET_USERNAME, CURRENT_USERNAME);
    }

    @Test
    void verifyOwnershipByUsername_ThrowAccessForbiddenException_UsernamesAreNotMatching(){
        assertThrows(AccessForbiddenException.class,
                () -> ownershipVerifier.verifyOwnershipByUsername(TARGET_USERNAME, "John.Doe1"));
    }
}
