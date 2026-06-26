package org.example.trainer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrainerRepositoryTest {

    private TrainerRepository trainerRepository;

    @BeforeEach
    void setUp() {
        trainerRepository = new TrainerRepository();
    }

    @Test
    void findByUsername_ReturnTrainer_UsernameExists() {
        Trainer trainer1 = new Trainer();
        trainer1.setUsername("Arnold.Schwarzenegger");
        Trainer trainer2 = new Trainer();
        trainer2.setUsername("Jane.Smith");
        trainerRepository.create(trainer1);
        trainerRepository.create(trainer2);
        Optional<Trainer> result = trainerRepository.findByUsername("Arnold.Schwarzenegger");
        assertTrue(result.isPresent(), "Trainer should be found in the storage");
        assertEquals("Arnold.Schwarzenegger", result.get().getUsername());
    }

    @Test
    void findByUsername_ReturnEmptyOptional_UsernameDoesNotExist() {
        Trainer trainer = new Trainer();
        trainer.setUsername("John.Doe");
        trainerRepository.create(trainer);
        Optional<Trainer> result = trainerRepository.findByUsername("Unknown.Trainer");
        assertTrue(result.isEmpty(), "Optional should be empty when the username is not found");
    }

    @Test
    void findByUsername_ReturnEmptyOptional_UsernameIsNull() {
        Trainer trainer = new Trainer();
        trainer.setUsername("John.Doe");
        trainerRepository.create(trainer);
        Optional<Trainer> result = trainerRepository.findByUsername(null);
        assertTrue(result.isEmpty(), "Optional should be empty when the passed username is null");
    }
}
