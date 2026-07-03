package org.example.trainer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrainerRepositoryTest {

    private TrainerRepository trainerRepository;

    @BeforeEach
    void setUp() {
        trainerRepository = new TrainerRepository();
        trainerRepository.initStorage(new ConcurrentHashMap<>());
    }

    @Test
    void findByUsername_ReturnTrainer_UsernameExists() {
        TrainerEntity trainer1 = new TrainerEntity();
        trainer1.setUsername("Arnold.Schwarzenegger");
        TrainerEntity trainer2 = new TrainerEntity();
        trainer2.setUsername("Jane.Smith");
        trainerRepository.create(trainer1);
        trainerRepository.create(trainer2);
        Optional<TrainerEntity> result = trainerRepository.findByUsername("Arnold.Schwarzenegger");
        assertTrue(result.isPresent(), "Trainer should be found in the storage");
        assertEquals("Arnold.Schwarzenegger", result.get().getUsername());
    }

    @Test
    void findByUsername_ReturnEmptyOptional_UsernameDoesNotExist() {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUsername("John.Doe");
        trainerRepository.create(trainer);
        Optional<TrainerEntity> result = trainerRepository.findByUsername("Unknown.Trainer");
        assertTrue(result.isEmpty(), "Optional should be empty when the username is not found");
    }
}
