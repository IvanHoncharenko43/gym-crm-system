package org.example.trainee.service;

import org.example.trainer.controller.response.Trainers;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.core.service.AuthenticationComponent;
import org.example.exception.EntityNotFoundException;
import org.example.trainee.controller.response.TraineeSummary;
import org.example.trainee.controller.request.UpdateTraineeRequest;
import org.example.trainee.controller.request.CreateTraineeRequest;
import org.example.trainee.controller.request.UpdateTraineeTrainersRequest;
import org.example.trainer.repository.TrainerEntity;
import org.example.trainer.repository.TrainerRepository;
import org.example.user.controller.dto.UserCredentials;
import org.example.core.service.GymMapper;
import org.example.trainee.repository.TraineeEntity;
import org.example.trainee.repository.TraineeRepository;
import org.example.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class TraineeService {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final GymMapper gymMapper;
    private final AuthenticationComponent authenticator;

    public TraineeService(TraineeRepository traineeRepository, TrainerRepository trainerRepository, UserRepository userRepository,
                          GymMapper gymMapper, AuthenticationComponent authenticator){
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.userRepository = userRepository;
        this.gymMapper = gymMapper;
        this.authenticator = authenticator;
    }

    @Transactional
    public TraineeSummary create(CreateTraineeRequest request) {
        String baseName = request.fullName().firstName() + "." + request.fullName().lastName();
        Set<String> existingUsernames = new HashSet<>(userRepository.findUsernamesByBaseNameForUpdate(baseName));
        TraineeEntity trainee = gymMapper.toTraineeEntity(request, existingUsernames);
        TraineeEntity savedTrainee = traineeRepository.save(trainee);
        log.info("Created trainee profile with ID: {}", savedTrainee.getId());
        return gymMapper.toTraineeSummary(savedTrainee);
    }

    @Transactional(readOnly = true)
    public TraineeSummary getById(Long id, UserCredentials credentials){
        authenticator.authenticate(credentials);
        log.info("Selecting trainee by id started");
        return traineeRepository.findById(id)
                .filter(trainee -> trainee.getUser().getIsActive())
                .map(gymMapper::toTraineeSummary)
                .orElseThrow(() -> {
                    String message = String.format("Trainee with ID %s not found or is inactive", id);
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
    }

    @Transactional(readOnly = true)
    public TraineeSummary getByUsername(String username, UserCredentials credentials){
        authenticator.authenticate(credentials);
        log.info("Selecting trainee by username started");
        return traineeRepository.findByUsername(username)
                .filter(trainee -> trainee.getUser().getIsActive())
                .map(gymMapper::toTraineeSummary)
                .orElseThrow(() -> {
                    String message = "Trainee not found or is inactive";
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
    }

    @Transactional
    public TraineeSummary update(Long id, UpdateTraineeRequest request, UserCredentials credentials) {
        authenticator.authenticate(credentials);
        TraineeEntity existingTrainee = traineeRepository.findById(id)
                .orElseThrow(() -> {
                    String message = String.format("Trainee with ID %s not found", id);
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
        authenticator.authorize(existingTrainee.getUser().getUsername(), credentials);
        TraineeEntity trainee = gymMapper.toTraineeEntity(request, existingTrainee);
        TraineeEntity updatedTrainee = traineeRepository.save(trainee);
        log.info("Updated trainee profile with ID: {}", updatedTrainee.getId());
        return gymMapper.toTraineeSummary(updatedTrainee);
    }

    @Transactional
    public void deleteByUsername(String username, UserCredentials credentials){
        authenticator.authenticate(credentials);
        Optional<TraineeEntity> existingTrainee = traineeRepository.findByUsername(username);
        if(existingTrainee.isPresent()) {
            authenticator.authorize(existingTrainee.get().getUser().getUsername(), credentials);
            traineeRepository.deleteByUserUsername(credentials.username());
            log.info("Deleted trainee profile by username");
        }
    }

    @Transactional
    public void changeActivity(Long id, UserCredentials credentials){
        authenticator.authenticate(credentials);
        TraineeEntity trainee = traineeRepository.findById(id)
                .orElseThrow(() -> {
                    String message = String.format("Trainee with ID %s not found", id);
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
        authenticator.authorize(trainee.getUser().getUsername(), credentials);
        trainee.getUser().setIsActive(!trainee.getUser().getIsActive());
        traineeRepository.save(trainee);
        log.info("Activity status changed for a trainee");
    }

    @Transactional
    public Trainers updateTrainersList(Long id, UpdateTraineeTrainersRequest request, UserCredentials credentials){
        authenticator.authenticate(credentials);
        TraineeEntity trainee = traineeRepository.findById(id)
                .orElseThrow(() -> {
                    String message = String.format("Trainee with ID %s not found", id);
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
        authenticator.authorize(trainee.getUser().getUsername(), credentials);
        Set<TrainerEntity> newTrainers = new HashSet<>(trainerRepository.findByUsernames(request.trainerUsernames()));
        trainee.getTrainers().stream()
                .filter(oldTrainer -> !newTrainers.contains(oldTrainer))
                .forEach(oldTrainer -> oldTrainer.getTrainees().remove(trainee));
        newTrainers.forEach(newTrainer -> newTrainer.getTrainees().add(trainee));
        trainee.getTrainers().addAll(newTrainers);
        traineeRepository.save(trainee);
        return new Trainers(
                newTrainers.stream()
                        .map(gymMapper::toTrainerSummary)
                        .toList()
        );
    }
}
