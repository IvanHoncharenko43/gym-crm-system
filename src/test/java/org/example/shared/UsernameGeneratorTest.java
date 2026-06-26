package org.example.shared;

import org.example.trainee.Trainee;
import org.example.trainee.TraineeRepository;
import org.example.trainer.Trainer;
import org.example.trainer.TrainerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsernameGeneratorTest {
    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @InjectMocks
    private UsernameGenerator usernameGenerator;

    @Test
    void generate_ReturnBaseUsername_NoCollisionsExist() {
        String firstName = "John";
        String lastName = "Doe";
        String expectedUsername = "John.Doe";
        when(trainerRepository.findByUsername(expectedUsername)).thenReturn(Optional.empty());
        when(traineeRepository.findByUsername(expectedUsername)).thenReturn(Optional.empty());

        String actualUsername = usernameGenerator.generate(firstName, lastName);
        assertEquals(expectedUsername, actualUsername, "Should return base username when it is unique");
        verify(trainerRepository, times(1)).findByUsername(expectedUsername);
        verify(traineeRepository, times(1)).findByUsername(expectedUsername);
    }

    @Test
    void generate_AppendSerialNumber1_BaseUsernameExistsInTrainees() {
        String firstName = "John";
        String lastName = "Doe";
        String baseUsername = "John.Doe";
        String expectedUsername = "John.Doe1";

        when(trainerRepository.findByUsername(baseUsername)).thenReturn(Optional.empty());
        when(traineeRepository.findByUsername(baseUsername)).thenReturn(Optional.of(new Trainee()));
        when(trainerRepository.findByUsername(expectedUsername)).thenReturn(Optional.empty());
        when(traineeRepository.findByUsername(expectedUsername)).thenReturn(Optional.empty());

        String actualUsername = usernameGenerator.generate(firstName, lastName);
        assertEquals(expectedUsername, actualUsername, "Should append '1' if base username exists");
    }

    @Test
    void generate_IncrementSerialNumber_MultipleCollisionsExist() {
        String firstName = "John";
        String lastName = "Doe";
        String baseUsername = "John.Doe";
        String collision1 = "John.Doe1";
        String expectedUsername = "John.Doe2";

        when(trainerRepository.findByUsername(baseUsername)).thenReturn(Optional.of(new Trainer()));
        when(trainerRepository.findByUsername(collision1)).thenReturn(Optional.empty());
        when(traineeRepository.findByUsername(collision1)).thenReturn(Optional.of(new Trainee()));
        when(trainerRepository.findByUsername(expectedUsername)).thenReturn(Optional.empty());
        when(traineeRepository.findByUsername(expectedUsername)).thenReturn(Optional.empty());

        String actualUsername = usernameGenerator.generate(firstName, lastName);
        assertEquals(expectedUsername, actualUsername, "Should correctly iterate and append '2'");
        verify(trainerRepository, times(1)).findByUsername(baseUsername);
        verify(trainerRepository, times(1)).findByUsername(collision1);
        verify(traineeRepository, times(1)).findByUsername(expectedUsername);
    }
}
