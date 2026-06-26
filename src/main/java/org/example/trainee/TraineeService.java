package org.example.trainee;

import lombok.extern.slf4j.Slf4j;
import org.example.shared.GymMapper;
import org.example.shared.PasswordGenerator;
import org.example.shared.UsernameGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class TraineeService {
    private TraineeRepository traineeRepository;
    private GymMapper gymMapper;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Autowired
    public void setGymMapper(GymMapper gymMapper) {
        this.gymMapper = gymMapper;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    public TraineeResponse create(CreateTraineeRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        if(request.firstName() == null || request.firstName().isBlank() ||
                request.lastName() == null || request.lastName().isBlank()) {
            throw new IllegalArgumentException("First and last names are required for registration");
        }
        Trainee trainee = gymMapper.toTrainee(request);
        String username = usernameGenerator.generate(trainee.getFirstName(), trainee.getLastName());
        String password = passwordGenerator.generate();
        trainee.setUsername(username);
        trainee.setPassword(password);
        Trainee savedTrainee = traineeRepository.create(trainee);
        log.info("Created trainee profile with ID: {}; and username: {}", savedTrainee.getId(), username);
        return gymMapper.toTraineeResponse(savedTrainee);
    }

    public TraineeResponse update(UpdateTraineeRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        if (request.id() == null) {
            throw new IllegalArgumentException("Trainee ID is required for update");
        }
        Trainee existingTrainee = traineeRepository.getById(request.id())
                .orElseThrow(() -> {
                    log.error("Trainee with ID {} not found", request.id());
                    return new IllegalArgumentException("Trainee with ID " + request.id() + " not found");
                });
        Trainee trainee = gymMapper.toTrainee(request);
        trainee.setUsername(existingTrainee.getUsername());
        trainee.setPassword(existingTrainee.getPassword());
        Trainee updatedTrainee = traineeRepository.update(trainee);
        log.info("Updated trainee profile with ID: {}", updatedTrainee.getId());
        return gymMapper.toTraineeResponse(updatedTrainee);
    }

    public TraineeResponse getById(Long id) {
        Objects.requireNonNull(id, "ID cannot be null");
        log.info("Selecting trainer by ID started");
        return traineeRepository.getById(id)
                .map(gymMapper::toTraineeResponse)
                .orElseThrow(() -> {
                    log.warn("Trainee with ID {} not found", id);
                    return new IllegalArgumentException("Trainee with ID " + id + " not found");
                });
    }

    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (traineeRepository.getById(id).isEmpty()) {
            log.error("Delete failed. Trainee with ID {} does not exist", id);
            throw new IllegalArgumentException("Trainee with ID " + id + " not found");
        }
        traineeRepository.deleteById(id);
        log.info("Deleted trainee profile with ID: {}", id);
    }
}
