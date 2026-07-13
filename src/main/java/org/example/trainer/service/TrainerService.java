package org.example.trainer.service;

import lombok.extern.slf4j.Slf4j;
import org.example.core.service.AuthenticationComponent;
import org.example.core.dto.ChangeActivityRequest;
import org.example.core.dto.ChangePasswordRequest;
import org.example.exception.InvalidPasswordException;
import org.example.exception.InvalidStatusTransitionException;
import org.example.exception.EntityNotFoundException;
import org.example.trainer.dto.UnassignedTrainersRequest;
import org.example.training.repository.TrainingTypeEntity;
import org.example.training.repository.TrainingTypeRepository;
import org.example.user.dto.UserCredentials;
import org.example.core.service.GymMapper;
import org.example.trainer.dto.TrainerSummary;
import org.example.trainer.dto.UpdateTrainerRequest;
import org.example.trainer.dto.CreateTrainerRequest;
import org.example.trainer.repository.TrainerEntity;
import org.example.trainer.repository.TrainerRepository;
import org.example.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
public class TrainerService {
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UserRepository userRepository;
    private final GymMapper gymMapper;
    private final AuthenticationComponent authComponent;

    public TrainerService(TrainerRepository trainerRepository, TrainingTypeRepository trainingTypeRepository, UserRepository userRepository,
                          GymMapper gymMapper, AuthenticationComponent authComponent){
        this.trainerRepository = trainerRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.userRepository = userRepository;
        this.gymMapper = gymMapper;
        this.authComponent = authComponent;
    }

    public TrainerSummary create(CreateTrainerRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        TrainingTypeEntity trainingType = trainingTypeRepository.findByName(request.specialization().trainingTypeName())
                .orElseThrow(() -> {
                    log.info("Training type {} not found", request.specialization().trainingTypeName());
                    return new EntityNotFoundException("Training type " + request.specialization().trainingTypeName() +
                            " not found");
                });
        String baseName = request.fullName().firstName() + "." + request.fullName().lastName();
        Set<String> existingUsernames = new HashSet<>(userRepository.findUsernamesByBaseName(baseName));
        TrainerEntity trainer = gymMapper.toTrainerEntity(request, trainingType, existingUsernames);
        TrainerEntity savedTrainer = trainerRepository.create(trainer);
        log.info("Created trainer profile with ID: {}", savedTrainer.getId());
        return gymMapper.toTrainerSummary(savedTrainer);
    }

    public TrainerSummary getByUsername(UserCredentials credentials){
        Objects.requireNonNull(credentials, "Credentials cannot be null");
        authComponent.authenticate(credentials);
        log.info("Selecting trainer by username started");
        String username = credentials.username();
        return trainerRepository.findByUsername(username)
                .filter(trainer -> trainer.getUser().getIsActive())
                .map(gymMapper::toTrainerSummary)
                .orElseThrow(() -> {
                    log.warn("Trainer with username {} not found or is inactive", username);
                    return new EntityNotFoundException("Trainer with username " + username + " not found or is inactive");
                });
    }

    public TrainerSummary update(UpdateTrainerRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        authComponent.authenticate(request.credentials());
        TrainerEntity existingTrainer = trainerRepository.getById(request.id())
                .orElseThrow(() -> {
                    log.error("Trainer with ID {} not found", request.id());
                    return new EntityNotFoundException("Trainer with ID " + request.id() + " not found");
                });
        TrainingTypeEntity trainingType = trainingTypeRepository.findByName(request.specialization().trainingTypeName())
                .orElseThrow(() -> {
                    log.info("Training type {} not found", request.specialization().trainingTypeName());
                    return new EntityNotFoundException("Training type " + request.specialization().trainingTypeName() +
                            " not found");
                });
        TrainerEntity trainer = gymMapper.toTrainerEntity(request, existingTrainer, trainingType);
        TrainerEntity updatedTrainer = trainerRepository.update(trainer);
        log.info("Updated trainer profile with ID: {}", updatedTrainer.getId());
        return gymMapper.toTrainerSummary(updatedTrainer);
    }

    public void changePassword(ChangePasswordRequest request){
        Objects.requireNonNull(request, "Request body cannot be null");
        authComponent.authenticate(request.credentials());
        TrainerEntity trainer = trainerRepository.findByUsername(request.credentials().username())
                .filter(t -> t.getUser().getIsActive())
                .orElseThrow(() -> {
                    log.error("Trainer with username not found or is inactive");
                    return new EntityNotFoundException("Trainer with username not found or is inactive");
                });
        if(request.newPassword().length() < 10){
            throw new InvalidPasswordException("Password should be at least 10 characters");
        }
        trainer.getUser().setPassword(request.newPassword());
        trainerRepository.update(trainer);
    }

    public void changeActivity(ChangeActivityRequest request){
        Objects.requireNonNull(request, "Request body cannot be null");
        authComponent.authenticate(request.credentials());
        TrainerEntity trainer = trainerRepository.findByUsername(request.credentials().username())
                .orElseThrow(() -> {
                    log.error("Trainer with username not found");
                    return new EntityNotFoundException("Trainer with username not found");
                });
        if(trainer.getUser().getIsActive() == request.isActive()){
            log.info("Cannot change status that is already assigned");
            throw new InvalidStatusTransitionException("Cannot change status that is already assigned");
        }
        trainer.getUser().setIsActive(request.isActive());
        trainerRepository.update(trainer);
        log.info("Activity status changed for a trainer");
    }

    public List<TrainerSummary> getUnassignedTrainersByTraineeList(UnassignedTrainersRequest request){
        Objects.requireNonNull(request, "Request body cannot be null");
        authComponent.authenticate(request.credentials());
        return trainerRepository.findUnassignedTrainersByTraineeUsername(request.traineeUsername()).stream()
                .filter(trainer -> trainer.getUser().getIsActive())
                .map(gymMapper::toTrainerSummary)
                .toList();
    }
}
