package org.example.crm.security.service;

import lombok.RequiredArgsConstructor;
import org.example.crm.exception.AccessForbiddenException;
import org.example.crm.trainee.repository.TraineeRepository;
import org.example.crm.trainer.repository.TrainerRepository;
import org.example.crm.training.repository.TrainingRepository;
import org.example.crm.user.controller.dto.UserRole;
import org.example.crm.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class OwnershipVerifier {

    private final UserRepository userRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;

    public enum ResourceType{
        USER,
        TRAINER,
        TRAINEE,
        TRAINING
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
            case TRAINING -> trainingRepository.existsByIdAndTrainerUserUsername(targetId, userDetails.getUsername());
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
