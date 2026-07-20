package org.example.trainee.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.core.service.AuthenticationComponent;
import org.example.core.dto.ChangeActivityRequest;
import org.example.core.dto.ChangePasswordRequest;
import org.example.exception.InvalidPasswordException;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class TraineeService {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final GymMapper gymMapper;
    private final AuthenticationComponent authenticator;
    private final TransactionTemplate transactionTemplate;
    private final ConcurrentHashMap<String, ReentrantLock> baseNameLocks = new ConcurrentHashMap<>();

    public TraineeService(TraineeRepository traineeRepository, TrainerRepository trainerRepository, UserRepository userRepository,
                          GymMapper gymMapper, AuthenticationComponent authenticator, TransactionTemplate transactionTemplate){
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.userRepository = userRepository;
        this.gymMapper = gymMapper;
        this.authenticator = authenticator;
        this.transactionTemplate = transactionTemplate;
    }

    public TraineeSummary create(CreateTraineeRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        String baseName = request.fullName().firstName() + "." + request.fullName().lastName();
        ReentrantLock lock = baseNameLocks.computeIfAbsent(baseName, l -> new ReentrantLock());
        lock.lock();
        try {
            return transactionTemplate.execute(status -> {
                Set<String> existingUsernames = new HashSet<>(userRepository.findUsernamesByBaseName(baseName));
                TraineeEntity trainee = gymMapper.toTraineeEntity(request, existingUsernames);
                TraineeEntity savedTrainee = traineeRepository.save(trainee);
                log.info("Created trainee profile with ID: {}", savedTrainee.getId());
                return gymMapper.toTraineeSummary(savedTrainee);
            });
        } finally {
            lock.unlock();
        }
    }

    @Transactional(readOnly = true)
    public TraineeSummary getByUsername(UserCredentials credentials){
        Objects.requireNonNull(credentials, "Credentials cannot be null");
        authenticator.authenticate(credentials);
        log.info("Selecting trainee by username started");
        String username = credentials.username();
        return traineeRepository.findByUsername(username)
                .filter(trainee -> trainee.getUser().getIsActive())
                .map(gymMapper::toTraineeSummary)
                .orElseThrow(() -> {
                    String message = "Trainee not found or is inactive";
                    return createEntityNotFoundException(message);
                });
    }

    @Transactional
    public TraineeSummary update(UpdateTraineeRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        authenticator.authenticate(request.credentials());
        TraineeEntity existingTrainee = traineeRepository.findById(request.id())
                .orElseThrow(() -> {
                    String message = String.format("Trainee with ID %s not found", request.id());
                    return createEntityNotFoundException(message);
                });
        TraineeEntity trainee = gymMapper.toTraineeEntity(request, existingTrainee);
        TraineeEntity updatedTrainee = traineeRepository.save(trainee);
        log.info("Updated trainee profile with ID: {}", updatedTrainee.getId());
        return gymMapper.toTraineeSummary(updatedTrainee);
    }

    @Transactional
    public void deleteByUsername(UserCredentials credentials){
        authenticator.authenticate(credentials);
        traineeRepository.deleteByUsername(credentials.username());
        log.info("Deleted trainee profile by username");
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request){
        Objects.requireNonNull(request, "Request body cannot be null");
        authenticator.authenticate(request.credentials());
        TraineeEntity trainee = traineeRepository.findByUsername(request.credentials().username())
                .filter(t -> t.getUser().getIsActive())
                .orElseThrow(() -> {
                    String message = "Trainee not found or is inactive";
                    return createEntityNotFoundException(message);
                });
        if(request.newPassword().length() < 10){
            throw new InvalidPasswordException("Password should be at least 10 characters");
        }
        trainee.getUser().setPassword(request.newPassword());
        traineeRepository.save(trainee);
    }

    @Transactional
    public void changeActivity(ChangeActivityRequest request){
        Objects.requireNonNull(request, "Request body cannot be null");
        authenticator.authenticate(request.credentials());
        TraineeEntity trainee = traineeRepository.findByUsername(request.credentials().username())
                .orElseThrow(() -> {
                    String message = "Trainee not found";
                    return createEntityNotFoundException(message);
                });
        trainee.getUser().setIsActive(!trainee.getUser().getIsActive());
        traineeRepository.save(trainee);
        log.info("Activity status changed for a trainee");
    }

    @Transactional
    public List<TrainerSummary> updateTrainersList(UpdateTraineeTrainersRequest request){
        Objects.requireNonNull(request, "Request body cannot be null");
        authenticator.authenticate(request.credentials());
        TraineeEntity trainee = traineeRepository.findByUsername(request.credentials().username())
                .orElseThrow(() -> {
                    String message = "Trainee not found";
                    return createEntityNotFoundException(message);
                });
        Set<TrainerEntity> newTrainers = new HashSet<>(trainerRepository.findByUsernames(request.trainerUsernames()));
        trainee.getTrainers().stream()
                .filter(oldTrainer -> !newTrainers.contains(oldTrainer))
                .forEach(oldTrainer -> oldTrainer.getTrainees().remove(trainee));
        newTrainers.forEach(newTrainer -> newTrainer.getTrainees().add(trainee));
        trainee.getTrainers().addAll(newTrainers);
        traineeRepository.save(trainee);
        return newTrainers.stream()
                .map(gymMapper::toTrainerSummary)
                .toList();
    }

    private EntityNotFoundException createEntityNotFoundException(String message){
        log.warn(message);
        return new EntityNotFoundException(message);
    }
}
