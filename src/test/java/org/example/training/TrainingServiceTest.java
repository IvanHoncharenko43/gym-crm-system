package org.example.training;

import org.example.component.GymMapper;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private GymMapper gymMapper;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void create_CreateAndReturnTrainingResponse_AllEntitiesExist() {
        CreateTrainingRequest request = mock(CreateTrainingRequest.class);
        when(request.traineeUsername()).thenReturn("John.Doe");
        when(request.trainerUsername()).thenReturn("Jane.Smith");
        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();
        Training mappedTraining = new Training();
        Training savedTraining = new Training();
        savedTraining.setId(1L);
        TrainingResponse expectedResponse = mock(TrainingResponse.class);

        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainer));
        when(gymMapper.toTraining(request, trainee, trainer)).thenReturn(mappedTraining);
        when(trainingRepository.create(mappedTraining)).thenReturn(savedTraining);
        when(gymMapper.toTrainingResponse(savedTraining, trainee, trainer)).thenReturn(expectedResponse);

        TrainingResponse actualResponse = trainingService.create(request);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(trainingRepository, times(1)).create(mappedTraining);
    }

    @Test
    void create_ThrowException_TraineeNotFound() {
        CreateTrainingRequest request = mock(CreateTrainingRequest.class);
        when(request.traineeUsername()).thenReturn("Unknown.Trainee");
        when(traineeRepository.findByUsername("Unknown.Trainee")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainingService.create(request));
        assertEquals("Trainee not found", exception.getMessage());
        verify(trainingRepository, never()).create(any());
    }

    @Test
    void create_ThrowException_TrainerNotFound() {
        CreateTrainingRequest request = mock(CreateTrainingRequest.class);
        when(request.traineeUsername()).thenReturn("John.Doe");
        when(request.trainerUsername()).thenReturn("Unknown.Trainer");
        Trainee trainee = new Trainee();
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername("Unknown.Trainer")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainingService.create(request));
        assertEquals("Trainer not found", exception.getMessage());
        verify(trainingRepository, never()).create(any());
    }

    @Test
    void getById_ReturnResponse_TrainingAndRelationsExist() {
        Long trainingId = 1L;
        Long traineeId = 2L;
        Long trainerId = 3L;
        Training training = new Training();
        training.setTraineeId(traineeId);
        training.setTrainerId(trainerId);
        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();
        TrainingResponse expectedResponse = mock(TrainingResponse.class);

        when(trainingRepository.getById(trainingId)).thenReturn(Optional.of(training));
        when(traineeRepository.getById(traineeId)).thenReturn(Optional.of(trainee));
        when(trainerRepository.getById(trainerId)).thenReturn(Optional.of(trainer));
        when(gymMapper.toTrainingResponse(training, trainee, trainer)).thenReturn(expectedResponse);

        TrainingResponse actualResponse = trainingService.getById(trainingId);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void getById_ThrowException_TrainingNotFound() {
        Long id = 99L;
        when(trainingRepository.getById(id)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainingService.getById(id));
        assertTrue(exception.getMessage().contains("not found"));
        verify(gymMapper, never()).toTrainingResponse(any(), any(), any());
    }

    @Test
    void getById_ThrowIllegalStateException_RelatedTraineeIsMissing() {
        Long trainingId = 1L;
        Long traineeId = 2L;
        Training training = new Training();
        training.setTraineeId(traineeId);
        when(trainingRepository.getById(trainingId)).thenReturn(Optional.of(training));
        when(traineeRepository.getById(traineeId)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> trainingService.getById(trainingId));
        assertEquals("Trainee for training not found", exception.getMessage());
    }

    @Test
    void getById_ThrowIllegalStateException_RelatedTrainerIsMissing() {
        Long trainingId = 1L;
        Long traineeId = 2L;
        Long trainerId = 3L;
        Training training = new Training();
        training.setTraineeId(traineeId);
        training.setTrainerId(trainerId);
        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();
        TrainingResponse expectedResponse = mock(TrainingResponse.class);

        when(trainingRepository.getById(trainingId)).thenReturn(Optional.of(training));
        when(traineeRepository.getById(traineeId)).thenReturn(Optional.of(trainee));
        when(trainerRepository.getById(trainerId)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> trainingService.getById(trainingId));
        assertEquals("Trainer for training not found", exception.getMessage());
    }
}
