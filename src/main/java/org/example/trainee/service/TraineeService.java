package org.example.trainee.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.core.service.AuthenticationComponent;
import org.example.core.dto.ChangeActivityRequest;
import org.example.core.dto.ChangePasswordRequest;
import org.example.exception.InvalidPasswordException;
import org.example.exception.InvalidStatusTransitionException;
import org.example.exception.EntityNotFoundException;
import org.example.trainee.dto.*;
import org.example.trainer.dto.TrainerSummary;
import org.example.trainer.repository.TrainerEntity;
import org.example.trainer.repository.TrainerRepository;
import org.example.user.dto.UserCredentials;
import org.example.core.service.GymMapper;
import org.example.trainee.repository.TraineeEntity;
import org.example.trainee.repository.TraineeRepository;
import org.example.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
public class TraineeService {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final GymMapper gymMapper;
    private final AuthenticationComponent authComponent;

    public TraineeService(TraineeRepository traineeRepository, TrainerRepository trainerRepository, UserRepository userRepository,
                          GymMapper gymMapper, AuthenticationComponent authComponent){
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.userRepository = userRepository;
        this.gymMapper = gymMapper;
        this.authComponent = authComponent;
    }

    @Transactional
    public TraineeSummary create(CreateTraineeRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        String baseName = request.fullName().firstName() + "." + request.fullName().lastName();
        Set<String> existingUsernames = new HashSet<>(userRepository.findUsernamesByBaseName(baseName));
        TraineeEntity trainee = gymMapper.toTraineeEntity(request, existingUsernames);
        TraineeEntity savedTrainee = traineeRepository.create(trainee);
        log.info("Created trainee profile with ID: {}", savedTrainee.getId());
        return gymMapper.toTraineeSummary(savedTrainee);
    }

    @Transactional
    public TraineeSummary getByUsername(UserCredentials credentials){
        Objects.requireNonNull(credentials, "Credentials cannot be null");
        authComponent.authenticate(credentials);
        log.info("Selecting trainee by username started");
        String username = credentials.username();
        return traineeRepository.findByUsername(username)
                .filter(trainee -> trainee.getUser().getIsActive())
                .map(gymMapper::toTraineeSummary)
                .orElseThrow(() -> {
                    log.warn("Trainee with username {} not found or is inactive", username);
                    return new EntityNotFoundException("Trainee with username " + username + " not found or is inactive");
                });
    }

    @Transactional
    public TraineeSummary update(UpdateTraineeRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        authComponent.authenticate(request.credentials());
        TraineeEntity existingTrainee = traineeRepository.getById(request.id())
                .orElseThrow(() -> {
                    log.error("Trainee with ID {} not found", request.id());
                    return new EntityNotFoundException("Trainee with ID " + request.id() + " not found");
                });
        TraineeEntity trainee = gymMapper.toTraineeEntity(request, existingTrainee);
        TraineeEntity updatedTrainee = traineeRepository.update(trainee);
        log.info("Updated trainee profile with ID: {}", updatedTrainee.getId());
        return gymMapper.toTraineeSummary(updatedTrainee);
    }

    @Transactional
    public void deleteByUsername(UserCredentials credentials){
        authComponent.authenticate(credentials);
        traineeRepository.deleteByUsername(credentials.username());
        log.info("Deleted trainee profile by username");
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request){
        Objects.requireNonNull(request, "Request body cannot be null");
        authComponent.authenticate(request.credentials());
        TraineeEntity trainee = traineeRepository.findByUsername(request.credentials().username())
                .filter(t -> t.getUser().getIsActive())
                .orElseThrow(() -> {
                    log.error("Trainee with username not found or is inactive");
                    return new EntityNotFoundException("Trainee with username not found or is inactive");
                });
        if(request.newPassword().length() < 10){
            throw new InvalidPasswordException("Password should be at least 10 characters");
        }
        trainee.getUser().setPassword(request.newPassword());
        traineeRepository.update(trainee);
    }

    @Transactional
    public void changeActivity(ChangeActivityRequest request){
        Objects.requireNonNull(request, "Request body cannot be null");
        authComponent.authenticate(request.credentials());
        TraineeEntity trainee = traineeRepository.findByUsername(request.credentials().username())
                .orElseThrow(() -> {
                    log.error("Trainee with username not found");
                    return new EntityNotFoundException("Trainee with username not found");
                });
        if(trainee.getUser().getIsActive() == request.isActive()){
            log.info("Cannot change status that is already assigned");
            throw new InvalidStatusTransitionException("Cannot change status that is already assigned");
        }
        trainee.getUser().setIsActive(request.isActive());
        traineeRepository.update(trainee);
        log.info("Activity status changed for a trainee");
    }

    @Transactional
    public List<TrainerSummary> updateTrainersList(UpdateTraineeTrainersRequest request){
        Objects.requireNonNull(request, "Request body cannot be null");
        authComponent.authenticate(request.credentials());
        TraineeEntity trainee = traineeRepository.findByUsername(request.credentials().username())
                .orElseThrow(() -> {
                    log.error("Trainee with username not found");
                    return new EntityNotFoundException("Trainee with username not found");
                });
        List<TrainerEntity> newTrainersList = trainerRepository.findByUsernames(request.trainerUsernames());
        Set<TrainerEntity> newTrainers = new HashSet<>(newTrainersList);
        for (TrainerEntity oldTrainer : trainee.getTrainers()) {
            if (!newTrainers.contains(oldTrainer)) {
                oldTrainer.getTrainees().remove(trainee);
            }
        }
        for (TrainerEntity newTrainer : newTrainers) {
            newTrainer.getTrainees().add(trainee);
        }
        trainee.setTrainers(newTrainers);
        traineeRepository.update(trainee);
        return newTrainersList.stream()
                .map(gymMapper::toTrainerSummary)
                .toList();
    }
}
