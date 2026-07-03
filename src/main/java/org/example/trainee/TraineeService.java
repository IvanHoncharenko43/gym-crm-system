package org.example.trainee;

import lombok.extern.slf4j.Slf4j;
import org.example.exception.NotFoundException;
import org.example.shared.GymMapper;
import org.example.shared.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class TraineeService {
    private TraineeRepository traineeRepository;
    private GymMapper gymMapper;

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Autowired
    public void setGymMapper(GymMapper gymMapper) {
        this.gymMapper = gymMapper;
    }

    public TraineeSummary create(CreateTraineeRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        Trainee trainee = gymMapper.toTraineeEntity(request);
        Trainee savedTrainee = traineeRepository.create(trainee);
        log.info("Created trainee profile with ID: {}", savedTrainee.getId());
        return gymMapper.toTraineeSummary(savedTrainee);
    }

    public TraineeSummary update(UpdateTraineeRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        Trainee existingTrainee = traineeRepository.getById(request.id())
                .orElseThrow(() -> {
                    log.error("Trainee with ID {} not found", request.id());
                    return new NotFoundException("Trainee with ID " + request.id() + " not found");
                });
        Trainee trainee = gymMapper.toTraineeEntity(request, existingTrainee.getUsername(), existingTrainee.getPassword());
        Trainee updatedTrainee = traineeRepository.update(trainee);
        log.info("Updated trainee profile with ID: {}", updatedTrainee.getId());
        return gymMapper.toTraineeSummary(updatedTrainee);
    }

    public TraineeSummary getById(Long id) {
        Objects.requireNonNull(id, "ID cannot be null");
        log.info("Selecting trainer by ID started");
        return traineeRepository.getById(id)
                .filter(User::isActive)
                .map(gymMapper::toTraineeSummary)
                .orElseThrow(() -> {
                    log.warn("Trainee with ID {} not found", id);
                    return new NotFoundException("Trainee with ID " + id + " not found");
                });
    }

    public void deleteById(Long id) {
        traineeRepository.deleteById(id);
        log.info("Deleted trainee profile with ID: {}", id);
    }
}
