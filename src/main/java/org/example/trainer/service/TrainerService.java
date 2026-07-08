package org.example.trainer.service;

import lombok.extern.slf4j.Slf4j;
import org.example.core.service.AuthenticationComponent;
import org.example.core.dto.ChangeActivityRequest;
import org.example.core.dto.ChangePasswordRequest;
import org.example.exception.InvalidPasswordException;
import org.example.exception.InvalidStatusTransitionException;
import org.example.exception.NotFoundException;
import org.example.trainer.dto.UnassignedTrainersRequest;
import org.example.user.dto.Credentials;
import org.example.core.service.GymMapper;
import org.example.trainer.dto.TrainerSummary;
import org.example.trainer.dto.UpdateTrainerRequest;
import org.example.trainer.dto.CreateTrainerRequest;
import org.example.trainer.repository.TrainerEntity;
import org.example.trainer.repository.TrainerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class TrainerService {
    private final TrainerRepository trainerRepository;
    private final GymMapper gymMapper;
    private final AuthenticationComponent authComponent;

    public TrainerService(TrainerRepository trainerRepository, GymMapper gymMapper, AuthenticationComponent authComponent){
        this.trainerRepository = trainerRepository;
        this.gymMapper = gymMapper;
        this.authComponent = authComponent;
    }

    public TrainerSummary create(CreateTrainerRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        TrainerEntity trainer = gymMapper.toTrainerEntity(request);
        TrainerEntity savedTrainer = trainerRepository.create(trainer);
        log.info("Created trainer profile with ID: {}", savedTrainer.getId());
        return gymMapper.toTrainerSummary(savedTrainer);
    }

    public TrainerSummary getByUsername(Credentials credentials){
        Objects.requireNonNull(credentials, "Credentials cannot be null");
        authComponent.authenticate(credentials);
        log.info("Selecting trainer by username started");
        String username = credentials.username();
        return trainerRepository.findByUsername(username)
                .filter(trainer -> trainer.getUser().getIsActive())
                .map(gymMapper::toTrainerSummary)
                .orElseThrow(() -> {
                    log.warn("Trainer with username {} not found or is inactive", username);
                    return new NotFoundException("Trainer with username " + username + " not found or is inactive");
                });
    }

    public TrainerSummary getById(Long id) {
        Objects.requireNonNull(id, "ID cannot be null");
        log.info("Selecting trainer by ID started");
        return trainerRepository.getById(id)
                .filter(trainer -> trainer.getUser().getIsActive())
                .map(gymMapper::toTrainerSummary)
                .orElseThrow(() -> {
                    log.warn("Trainer with ID {} not found or is inactive", id);
                    return new NotFoundException("Trainer with ID " + id + " not found or is inactive");
                });
    }

    public TrainerSummary update(UpdateTrainerRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        TrainerEntity existingTrainer = trainerRepository.getById(request.id())
                .orElseThrow(() -> {
                    log.error("Trainer with ID {} not found", request.id());
                    return new NotFoundException("Trainer with ID " + request.id() + " not found");
                });
        TrainerEntity trainer = gymMapper.toTrainerEntity(request, existingTrainer);
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
                    return new NotFoundException("Trainer with username not found or is inactive");
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
                    return new NotFoundException("Trainer with username not found");
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
