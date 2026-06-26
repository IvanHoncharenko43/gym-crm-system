package org.example.trainer;

import lombok.extern.slf4j.Slf4j;
import org.example.shared.GymMapper;
import org.example.shared.PasswordGenerator;
import org.example.shared.UsernameGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class TrainerService {
    private TrainerRepository trainerRepository;
    private GymMapper gymMapper;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository){
        this.trainerRepository = trainerRepository;
    }

    @Autowired
    public void setGymMapper(GymMapper gymMapper){
        this.gymMapper = gymMapper;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator){
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator){
        this.passwordGenerator = passwordGenerator;
    }

    public TrainerResponse create(CreateTrainerRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        if(request.firstName() == null || request.firstName().isBlank() ||
                request.lastName() == null || request.lastName().isBlank()) {
            throw new IllegalArgumentException("First and last names are required for registration");
        }
        Trainer trainer = gymMapper.toTrainer(request);
        String username = usernameGenerator.generate(trainer.getFirstName(), trainer.getLastName());
        String password = passwordGenerator.generate();
        trainer.setUsername(username);
        trainer.setPassword(password);
        Trainer savedTrainer = trainerRepository.create(trainer);
        log.info("Created trainer profile with ID: {} and username: {}", savedTrainer.getId(), username);
        return gymMapper.toTrainerResponse(savedTrainer);
    }

    public TrainerResponse update(UpdateTrainerRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        if (request.id() == null) {
            throw new IllegalArgumentException("Trainer ID is required for update");
        }
        Trainer existingTrainer = trainerRepository.getById(request.id())
                .orElseThrow(() -> {
                    log.error("Trainer with ID {} not found", request.id());
                    return new IllegalArgumentException("Trainer with ID " + request.id() + " not found");
                });
        Trainer trainer = gymMapper.toTrainer(request);
        trainer.setUsername(existingTrainer.getUsername());
        trainer.setPassword(existingTrainer.getPassword());
        Trainer updatedTrainer = trainerRepository.update(trainer);
        log.info("Successfully updated trainer profile with ID: {}", updatedTrainer.getId());
        return gymMapper.toTrainerResponse(updatedTrainer);
    }

    public TrainerResponse getById(Long id) {
        Objects.requireNonNull(id, "ID cannot be null");
        log.info("Selecting trainer by ID started");
        return trainerRepository.getById(id)
                .map(gymMapper::toTrainerResponse)
                .orElseThrow(() -> {
                    log.warn("Trainer with ID {} not found", id);
                    return new IllegalArgumentException("Trainer with ID " + id + " not found");
                });
    }
}
