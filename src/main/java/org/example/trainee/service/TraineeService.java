package org.example.trainee.service;

import lombok.extern.slf4j.Slf4j;
import org.example.core.service.AuthenticationComponent;
import org.example.core.dto.ChangeActivityRequest;
import org.example.core.dto.ChangePasswordRequest;
import org.example.exception.InvalidPasswordException;
import org.example.exception.InvalidStatusTransitionException;
import org.example.exception.NotFoundException;
import org.example.trainee.dto.*;
import org.example.trainer.dto.TrainerSummary;
import org.example.trainer.repository.TrainerEntity;
import org.example.trainer.repository.TrainerRepository;
import org.example.user.dto.Credentials;
import org.example.core.service.GymMapper;
import org.example.trainee.repository.TraineeEntity;
import org.example.trainee.repository.TraineeRepository;
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
    private final GymMapper gymMapper;
    private final AuthenticationComponent authComponent;

    public TraineeService(TraineeRepository traineeRepository, TrainerRepository trainerRepository,
                          GymMapper gymMapper, AuthenticationComponent authComponent){
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.gymMapper = gymMapper;
        this.authComponent = authComponent;
    }

    public TraineeSummary create(CreateTraineeRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        TraineeEntity trainee = gymMapper.toTraineeEntity(request);
        TraineeEntity savedTrainee = traineeRepository.create(trainee);
        log.info("Created trainee profile with ID: {}", savedTrainee.getId());
        return gymMapper.toTraineeSummary(savedTrainee);
    }

    public TraineeSummary getByUsername(Credentials credentials){
        Objects.requireNonNull(credentials, "Credentials cannot be null");
        authComponent.authenticate(credentials);
        log.info("Selecting trainee by username started");
        String username = credentials.username();
        return traineeRepository.findByUsername(username)
                .filter(trainee -> trainee.getUser().getIsActive())
                .map(gymMapper::toTraineeSummary)
                .orElseThrow(() -> {
                    log.warn("Trainee with username {} not found or is inactive", username);
                    return new NotFoundException("Trainee with username " + username + " not found or is inactive");
                });
    }

    public TraineeSummary update(UpdateTraineeRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        authComponent.authenticate(request.credentials());
        TraineeEntity existingTrainee = traineeRepository.getById(request.id())
                .orElseThrow(() -> {
                    log.error("Trainee with ID {} not found", request.id());
                    return new NotFoundException("Trainee with ID " + request.id() + " not found");
                });
        TraineeEntity trainee = gymMapper.toTraineeEntity(request, existingTrainee);
        TraineeEntity updatedTrainee = traineeRepository.update(trainee);
        log.info("Updated trainee profile with ID: {}", updatedTrainee.getId());
        return gymMapper.toTraineeSummary(updatedTrainee);
    }

//    public TraineeSummary getById(Long id) {
//        Objects.requireNonNull(id, "ID cannot be null");
////        authComponent.authenticate();
//        log.info("Selecting trainee by ID started");
//        return traineeRepository.getById(id)
//                .filter(UserEntity::isActive)
//                .map(gymMapper::toTraineeSummary)
//                .orElseThrow(() -> {
//                    log.warn("Trainee with ID {} not found or is inactive", id);
//                    return new NotFoundException("Trainee with ID " + id + " not found or is inactive");
//                });
//    }

    public void deleteByUsername(Credentials credentials){
        authComponent.authenticate(credentials);
        traineeRepository.deleteByUsername(credentials.username());
        log.info("Deleted trainee profile by username");
    }

//    public void deleteById(Long id) {
//        traineeRepository.deleteById(id);
//        log.info("Deleted trainee profile with ID: {}", id);
//    }

    public void changePassword(ChangePasswordRequest request){
        Objects.requireNonNull(request, "Request body cannot be null");
        authComponent.authenticate(request.credentials());
        TraineeEntity trainee = traineeRepository.findByUsername(request.credentials().username())
                .filter(t -> t.getUser().getIsActive())
                .orElseThrow(() -> {
                    log.error("Trainee with username not found or is inactive");
                    return new NotFoundException("Trainee with username not found or is inactive");
                });
        if(request.newPassword().length() < 10){
            throw new InvalidPasswordException("Password should be at least 10 characters");
        }
        trainee.getUser().setPassword(request.credentials().password());
        traineeRepository.update(trainee);
    }

    public void changeActivity(ChangeActivityRequest request){
        Objects.requireNonNull(request, "Request body cannot be null");
        authComponent.authenticate(request.credentials());
        TraineeEntity trainee = traineeRepository.findByUsername(request.credentials().username())
                .orElseThrow(() -> {
                    log.error("Trainee with username not found");
                    return new NotFoundException("Trainee with username not found");
                });
        if(trainee.getUser().getIsActive() == request.isActive()){
            log.info("Cannot change status that is already assigned");
            throw new InvalidStatusTransitionException("Cannot change status that is already assigned");
        }
        trainee.getUser().setIsActive(request.isActive());
        traineeRepository.update(trainee);
        log.info("Activity status changed for a trainee");
    }

    public List<TrainerSummary> updateTrainersList(UpdateTraineeTrainersRequest request){
        Objects.requireNonNull(request, "Request body cannot be null");
        authComponent.authenticate(request.credentials());
        TraineeEntity trainee = traineeRepository.findByUsername(request.credentials().username())
                .orElseThrow(() -> {
                    log.error("Trainee with username not found");
                    return new NotFoundException("Trainee with username not found");
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
