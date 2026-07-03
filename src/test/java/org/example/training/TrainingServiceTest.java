package org.example.training;

import org.example.TestUtils;
import org.example.exception.NotFoundException;
import org.example.shared.GymMapper;
import org.example.shared.TrainingType;
import org.example.trainee.TraineeEntity;
import org.example.trainee.TraineeRepository;
import org.example.trainer.TrainerEntity;
import org.example.trainer.TrainerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainingServiceTest {

    private static final Long TRAINING_ID = 1L;
    private static final Long TRAINER_ID = 2L;
    private static final Long TRAINEE_ID = 3L;

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
        CreateTrainingRequest request = new CreateTrainingRequest(
                TRAINER_ID, TRAINEE_ID, "Cardio",
                LocalDate.of(2026, 5, 12), 45
        );
        TraineeEntity trainee = new TraineeEntity();
        TrainerEntity trainer = new TrainerEntity();
        TrainingEntity mappedTraining = new TrainingEntity();
        TrainingEntity savedTraining = new TrainingEntity();
        savedTraining.setId(TRAINING_ID);
        TrainingSummary expectedResponse = new TrainingSummary(
                TRAINING_ID,
                TestUtils.getTrainerSummary(TRAINER_ID),
                TestUtils.getTraineeSummary(TRAINEE_ID),
                "Cardio",
                TrainingType.YOGA,
                LocalDate.of(2026, 5, 12), 45
        );

        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.of(trainee));
        when(trainerRepository.getById(TRAINER_ID)).thenReturn(Optional.of(trainer));
        when(gymMapper.toTraining(request, trainee, trainer)).thenReturn(mappedTraining);
        when(trainingRepository.create(mappedTraining)).thenReturn(savedTraining);
        when(gymMapper.toTrainingSummary(savedTraining, trainee, trainer)).thenReturn(expectedResponse);

        TrainingSummary actualResponse = trainingService.create(request);

        assertEquals(expectedResponse, actualResponse);
        verify(traineeRepository, times(1)).getById(TRAINEE_ID);
        verify(trainerRepository, times(1)).getById(TRAINER_ID);
        verify(gymMapper, times(1)).toTraining(request, trainee, trainer);
        verify(trainingRepository, times(1)).create(mappedTraining);
        verify(gymMapper, times(1)).toTrainingSummary(savedTraining, trainee, trainer);
    }

    @Test
    void create_ThrowNotFoundException_TraineeNotFound() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TRAINER_ID, TRAINEE_ID, "Cardio",
                LocalDate.of(2026, 5, 12), 45
        );

        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> trainingService.create(request));
        assertTrue(exception.getMessage().contains("Trainee not found"));
        verify(traineeRepository, times(1)).getById(TRAINEE_ID);
        verify(trainingRepository, never()).create(any());
    }

    @Test
    void create_ThrowNotFoundException_TrainerNotFound() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TRAINER_ID, TRAINEE_ID, "Cardio",
                LocalDate.of(2026, 5, 12), 45
        );
        TraineeEntity trainee = new TraineeEntity();

        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.of(trainee));
        when(trainerRepository.getById(TRAINER_ID)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> trainingService.create(request));
        assertTrue(exception.getMessage().contains("Trainer not found"));
        verify(traineeRepository, times(1)).getById(TRAINEE_ID);
        verify(trainerRepository, times(1)).getById(TRAINER_ID);
        verify(trainingRepository, never()).create(any());
    }

    @Test
    void getById_ReturnResponse_TrainingAndRelationsExist() {
        TrainingEntity training = new TrainingEntity();
        training.setTraineeId(TRAINEE_ID);
        training.setTrainerId(TRAINER_ID);
        TraineeEntity trainee = new TraineeEntity();
        TrainerEntity trainer = new TrainerEntity();
        TrainingSummary expectedResponse = new TrainingSummary(
                TRAINING_ID,
                TestUtils.getTrainerSummary(TRAINER_ID),
                TestUtils.getTraineeSummary(TRAINEE_ID),
                "Cardio",
                TrainingType.YOGA,
                LocalDate.of(2026, 5, 12), 45
        );

        when(trainingRepository.getById(TRAINING_ID)).thenReturn(Optional.of(training));
        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.of(trainee));
        when(trainerRepository.getById(TRAINER_ID)).thenReturn(Optional.of(trainer));
        when(gymMapper.toTrainingSummary(training, trainee, trainer)).thenReturn(expectedResponse);

        TrainingSummary actualResponse = trainingService.getById(TRAINING_ID);
        assertEquals(expectedResponse, actualResponse);
        verify(trainingRepository, times(1)).getById(TRAINING_ID);
        verify(traineeRepository, times(1)).getById(TRAINEE_ID);
        verify(trainerRepository, times(1)).getById(TRAINER_ID);
        verify(gymMapper, times(1)).toTrainingSummary(training, trainee, trainer);
    }

    @Test
    void getById_ThrowNotFoundException_TrainingIsMissing() {
        when(trainingRepository.getById(TRAINING_ID)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> trainingService.getById(TRAINING_ID));

        assertTrue(exception.getMessage().contains("Training not found"));
        verify(trainingRepository, times(1)).getById(TRAINING_ID);
        verify(gymMapper, never()).toTrainingSummary(any(), any(), any());
    }

    @Test
    void getById_ThrowNotFoundException_RelatedTraineeIsMissing() {
        TrainingEntity training = new TrainingEntity();
        training.setTraineeId(TRAINING_ID);
        when(trainingRepository.getById(TRAINING_ID)).thenReturn(Optional.of(training));
        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> trainingService.getById(TRAINING_ID));

        assertEquals("Trainee for training not found", exception.getMessage());
        verify(trainingRepository, times(1)).getById(TRAINING_ID);
        verify(traineeRepository, times(1)).getById(TRAINEE_ID);
        verify(gymMapper, never()).toTrainingSummary(any(), any(), any());
    }

    @Test
    void getById_ThrowNotFoundException_RelatedTrainerIsMissing() {
        TrainingEntity training = new TrainingEntity();
        training.setTraineeId(TRAINEE_ID);
        training.setTrainerId(TRAINER_ID);
        TraineeEntity trainee = new TraineeEntity();

        when(trainingRepository.getById(TRAINING_ID)).thenReturn(Optional.of(training));
        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.of(trainee));
        when(trainerRepository.getById(TRAINER_ID)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> trainingService.getById(TRAINING_ID));

        assertEquals("Trainer for training not found", exception.getMessage());
        verify(trainingRepository, times(1)).getById(TRAINING_ID);
        verify(traineeRepository, times(1)).getById(TRAINEE_ID);
        verify(trainerRepository, times(1)).getById(TRAINER_ID);
        verify(gymMapper, never()).toTrainingSummary(any(), any(), any());
    }
}
