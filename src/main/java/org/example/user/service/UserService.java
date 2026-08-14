package org.example.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.core.dto.ChangePasswordRequest;
import org.example.security.service.OwnershipVerifier;
import org.example.exception.EntityNotFoundException;
import org.example.exception.InvalidPasswordException;
import org.example.user.repository.UserEntity;
import org.example.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OwnershipVerifier ownershipVerifier;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request, UserDetails userDetails){
        UserEntity user = userRepository.findById(id)
                .filter(UserEntity::getIsActive)
                .orElseThrow(() -> {
                    String message = String.format("User with ID %s not found or is inactive", id);
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
        ownershipVerifier.verifyOwnershipByUsername(user.getUsername(), userDetails.getUsername());
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Changed password for a user with ID: {}", id);
    }
}
