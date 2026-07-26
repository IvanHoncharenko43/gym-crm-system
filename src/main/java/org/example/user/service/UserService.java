package org.example.user.service;

import lombok.extern.slf4j.Slf4j;
import org.example.core.dto.ChangePasswordRequest;
import org.example.core.service.AuthenticationComponent;
import org.example.exception.EntityNotFoundException;
import org.example.user.controller.dto.UserCredentials;
import org.example.user.repository.UserEntity;
import org.example.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final AuthenticationComponent authenticator;

    public UserService(UserRepository userRepository, AuthenticationComponent authenticator){
        this.userRepository = userRepository;
        this.authenticator = authenticator;
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, UserCredentials credentials){
        authenticator.authenticate(credentials);
        authenticator.authorize(request.username(), credentials);
        UserEntity user = userRepository.findByUsername(request.username())
                .filter(UserEntity::getIsActive)
                .orElseThrow(() -> {
                    String message = "User not found or is inactive";
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
        user.setPassword(request.newPassword());
        userRepository.save(user);
    }
}
