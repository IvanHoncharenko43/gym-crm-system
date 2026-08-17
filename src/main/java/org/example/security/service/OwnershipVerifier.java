package org.example.security.service;

import lombok.RequiredArgsConstructor;
import org.example.exception.AccessForbiddenException;
import org.example.trainee.repository.TraineeRepository;
import org.example.trainer.repository.TrainerRepository;
import org.example.user.controller.dto.UserRole;
import org.example.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class OwnershipVerifier {

    private final UserRepository userRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    public enum ResourceType{
        USER,
        TRAINER,
        TRAINEE
    }

    public void verifyOwnership(String targetUsername, UserDetails userDetails){
        if (isAdmin(userDetails)){
            return;
        }
        if(!targetUsername.equals(userDetails.getUsername())){
            throw new AccessForbiddenException("Authorization failed");
        }
    }

    public void verifyOwnership(Long targetId, UserDetails userDetails, ResourceType resourceType){
        if (isAdmin(userDetails)) {
            return;
        }
        boolean exists = switch (resourceType) {
            case USER -> userRepository.existsByIdAndUsername(targetId, userDetails.getUsername());
            case TRAINEE -> traineeRepository.existsByIdAndUserUsername(targetId, userDetails.getUsername());
            case TRAINER -> trainerRepository.existsByIdAndUserUsername(targetId, userDetails.getUsername());
        };
        if (!exists) {
            throw new AccessForbiddenException("Authorization failed");
        }
    }

    private boolean isAdmin(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), UserRole.ADMIN.getAuthority()));
    }
}
