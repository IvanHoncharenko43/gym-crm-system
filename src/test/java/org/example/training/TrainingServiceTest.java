package org.example.training;

import org.example.TestUtils;
import org.example.core.service.AuthenticationComponent;
import org.example.exception.EntityNotFoundException;
import org.example.core.service.GymMapper;
import org.example.trainee.dto.GetTraineeTrainingsRequest;
import org.example.trainee.repository.TraineeEntity;
import org.example.trainee.repository.TraineeRepository;
import org.example.trainer.repository.TrainerEntity;
import org.example.trainer.repository.TrainerRepository;
import org.example.training.dto.CreateTrainingRequest;
import org.example.training.dto.GetTrainingRequest;
import org.example.training.dto.TrainingSummary;
import org.example.training.repository.TrainingEntity;
import org.example.training.repository.TrainingRepository;
import org.example.training.service.TrainingService;
import org.example.user.repository.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
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

    @Mock
    private AuthenticationComponent authComponent;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void create_CreateAndReturnTrainingResponse_AllEntitiesValid() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TestUtils.getTraineeCredentials(), TRAINER_ID, TRAINEE_ID, "Cardio",
                LocalDate.of(2026, 5, 12), 45
        );
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(new UserEntity());
        trainee.getUser().setIsActive(true);
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUser(new UserEntity());
        trainer.getUser().setIsActive(true);
        TrainingEntity training = new TrainingEntity();
        training.setId(TRAINING_ID);
        TrainingSummary expectedResponse = new TrainingSummary(
                TRAINING_ID,
                TestUtils.getTrainerSummary(TRAINER_ID),
                TestUtils.getTraineeSummary(TRAINEE_ID),
                "Cardio",
                TestUtils.getTrainingTypeSummary(),
                LocalDate.of(2026, 5, 12), 45
        );

        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.of(trainee));
        when(trainerRepository.getById(TRAINER_ID)).thenReturn(Optional.of(trainer));
        when(gymMapper.toTraining(request, trainee, trainer)).thenReturn(training);
        when(trainingRepository.create(training)).thenReturn(training);
        when(gymMapper.toTrainingSummary(training, trainee, trainer)).thenReturn(expectedResponse);

        TrainingSummary actualResponse = trainingService.create(request);

        assertEquals(expectedResponse, actualResponse);
        verify(authComponent, times(1)).authenticate(request.credentials());
        verify(traineeRepository, times(1)).getById(TRAINEE_ID);
        verify(trainerRepository, times(1)).getById(TRAINER_ID);
        verify(gymMapper, times(1)).toTraining(request, trainee, trainer);
        verify(trainingRepository, times(1)).create(training);
        verify(gymMapper, times(1)).toTrainingSummary(training, trainee, trainer);
    }

    @Test
    void create_ThrowEntityNotFoundException_TraineeNotFound() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TestUtils.getTraineeCredentials(), TRAINER_ID, TRAINEE_ID, "Cardio",
                LocalDate.of(2026, 5, 12), 45
        );

        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> trainingService.create(request));
        assertTrue(exception.getMessage().contains("Trainee"));
        verify(authComponent, times(1)).authenticate(request.credentials());
        verify(traineeRepository, times(1)).getById(TRAINEE_ID);
        verify(trainingRepository, never()).create(any());
    }

    @Test
    void create_ThrowEntityNotFoundException_TraineeIsInactive() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TestUtils.getTraineeCredentials(), TRAINER_ID, TRAINEE_ID, "Cardio",
                LocalDate.of(2026, 5, 12), 45
        );
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(new UserEntity());
        trainee.getUser().setIsActive(false);

        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.of(trainee));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> trainingService.create(request));
        assertTrue(exception.getMessage().contains("Trainee"));
        verify(authComponent, times(1)).authenticate(request.credentials());
        verify(traineeRepository, times(1)).getById(TRAINEE_ID);
        verify(trainingRepository, never()).create(any());
    }

    @Test
    void create_ThrowEntityNotFoundException_TrainerNotFound() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TestUtils.getTraineeCredentials(), TRAINER_ID, TRAINEE_ID, "Cardio",
                LocalDate.of(2026, 5, 12), 45
        );
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(new UserEntity());
        trainee.getUser().setIsActive(true);

        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.of(trainee));
        when(trainerRepository.getById(TRAINER_ID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> trainingService.create(request));
        assertTrue(exception.getMessage().contains("Trainer"));
        verify(authComponent, times(1)).authenticate(request.credentials());
        verify(traineeRepository, times(1)).getById(TRAINEE_ID);
        verify(trainerRepository, times(1)).getById(TRAINER_ID);
        verify(trainingRepository, never()).create(any());
    }

    @Test
    void create_ThrowEntityNotFoundException_TrainerIsInactive() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TestUtils.getTraineeCredentials(), TRAINER_ID, TRAINEE_ID, "Cardio",
                LocalDate.of(2026, 5, 12), 45
        );
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(new UserEntity());
        trainee.getUser().setIsActive(true);
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUser(new UserEntity());
        trainer.getUser().setIsActive(false);

        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.of(trainee));
        when(trainerRepository.getById(TRAINER_ID)).thenReturn(Optional.of(trainer));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> trainingService.create(request));
        assertTrue(exception.getMessage().contains("Trainer"));
        verify(authComponent, times(1)).authenticate(request.credentials());
        verify(traineeRepository, times(1)).getById(TRAINEE_ID);
        verify(trainerRepository, times(1)).getById(TRAINER_ID);
        verify(trainingRepository, never()).create(any());
    }

    @Test
    void getById_ReturnResponse_TrainingAndRelationsExist() {
        GetTrainingRequest request = new GetTrainingRequest(TestUtils.getTraineeCredentials(), TRAINING_ID);
        TrainingEntity training = new TrainingEntity();
        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(TRAINEE_ID);
        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(TRAINER_ID);
        training.setTrainee(trainee);
        training.setTrainer(trainer);

        TrainingSummary expectedResponse = new TrainingSummary(
                TRAINING_ID,
                TestUtils.getTrainerSummary(TRAINER_ID),
                TestUtils.getTraineeSummary(TRAINEE_ID),
                "Cardio",
                TestUtils.getTrainingTypeSummary(),
                LocalDate.of(2026, 5, 12), 45
        );

        when(trainingRepository.getById(TRAINING_ID)).thenReturn(Optional.of(training));
        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.of(trainee));
        when(trainerRepository.getById(TRAINER_ID)).thenReturn(Optional.of(trainer));
        when(gymMapper.toTrainingSummary(training, trainee, trainer)).thenReturn(expectedResponse);

        TrainingSummary actualResponse = trainingService.getById(request);
        assertEquals(expectedResponse, actualResponse);
        verify(authComponent, times(1)).authenticate(request.credentials());
        verify(trainingRepository, times(1)).getById(TRAINING_ID);
        verify(traineeRepository, times(1)).getById(TRAINEE_ID);
        verify(trainerRepository, times(1)).getById(TRAINER_ID);
        verify(gymMapper, times(1)).toTrainingSummary(training, trainee, trainer);
    }

    @Test
    void getById_ThrowEntityNotFoundException_TrainingIsMissing() {
        GetTrainingRequest request = new GetTrainingRequest(TestUtils.getTraineeCredentials(), TRAINING_ID);
        when(trainingRepository.getById(TRAINING_ID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> trainingService.getById(request));

        assertTrue(exception.getMessage().contains("Training"));
        verify(authComponent, times(1)).authenticate(request.credentials());
        verify(trainingRepository, times(1)).getById(TRAINING_ID);
        verify(gymMapper, never()).toTrainingSummary(any(), any(), any());
    }

//    @Test
//    void getById_ThrowEntityNotFoundException_RelatedTraineeIsMissing() {
//        TrainingEntity training = new TrainingEntity();
//        training.setTrainee(new TraineeEntity());
//        training.getTrainee().setId(TRAINEE_ID);
//        when(trainingRepository.getById(TRAINING_ID)).thenReturn(Optional.of(training));
//        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.empty());
//
//        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
//                () -> trainingService.getById(TRAINING_ID));
//
//        assertTrue(exception.getMessage().contains("Trainee"));
//        verify(trainingRepository, times(1)).getById(TRAINING_ID);
//        verify(traineeRepository, times(1)).getById(TRAINEE_ID);
//        verify(gymMapper, never()).toTrainingSummary(any(), any(), any());
//    }
//
//    @Test
//    void getById_ThrowEntityNotFoundException_RelatedTrainerIsMissing() {
//        TrainingEntity training = new TrainingEntity();
//        training.getTrainee().setId(TRAINEE_ID);
//        training.getTrainer().setId(TRAINER_ID);
//        TraineeEntity trainee = new TraineeEntity();
//
//        when(trainingRepository.getById(TRAINING_ID)).thenReturn(Optional.of(training));
//        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.of(trainee));
//        when(trainerRepository.getById(TRAINER_ID)).thenReturn(Optional.empty());
//
//        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
//                () -> trainingService.getById(TRAINING_ID));
//
//        assertTrue(exception.getMessage().contains("Trainer"));
//        verify(trainingRepository, times(1)).getById(TRAINING_ID);
//        verify(traineeRepository, times(1)).getById(TRAINEE_ID);
//        verify(trainerRepository, times(1)).getById(TRAINER_ID);
//        verify(gymMapper, never()).toTrainingSummary(any(), any(), any());
//    }

    @Test
    void getTraineeTrainingList_ReturnTrainingsList_RequestIsValid(){
        GetTraineeTrainingsRequest request = new GetTraineeTrainingsRequest(
                TestUtils.getTraineeCredentials(),
                LocalDate.of(2023, 12, 3),
                LocalDate.of(2026, 5, 12),
                "Doe",
                TestUtils.getTrainingTypeSummary());
        TraineeEntity trainee1 = new TraineeEntity();
        trainee1.setId(1L);
        TrainerEntity trainer1 = new TrainerEntity();
        trainer1.setId(1L);
        TrainerEntity trainer2 = new TrainerEntity();
        trainer2.setId(2L);
        TrainingEntity training1 = new TrainingEntity();
        trainee1.setId(1L);
        training1.setTrainee(trainee1);
        training1.setTrainer(trainer1);
        TrainingEntity training2 = new TrainingEntity();
        training2.setId(2L);
        training2.setTrainee(trainee1);
        training2.setTrainer(trainer2);
        List<TrainingEntity> trainings = List.of(training1, training2);
        TrainingSummary trainingSummary1 = new TrainingSummary(
                1L,
                TestUtils.getTrainerSummary(TRAINER_ID),
                TestUtils.getTraineeSummary(TRAINEE_ID),
                "Cardio",
                TestUtils.getTrainingTypeSummary(),
                LocalDate.of(2024, 5, 12), 45
        );
        TrainingSummary trainingSummary2 = new TrainingSummary(
                2L,
                TestUtils.getTrainerSummary(TRAINER_ID),
                TestUtils.getTraineeSummary(TRAINEE_ID),
                "Cardio",
                TestUtils.getTrainingTypeSummary(),
                LocalDate.of(2026, 5, 12), 45
        );
        List<TrainingSummary> mappedTrainings = List.of(trainingSummary1, trainingSummary2);

        when(trainingRepository.findTraineeTrainingsByCriteria(
                request.credentials().username(),
                request.fromDate(),
                request.toDate(),
                request.trainerName(),
                request.trainingType().trainingTypeName()
        )).thenReturn(trainings);
        when(gymMapper.toTrainingSummary(training1, trainee1, trainer1)).thenReturn(trainingSummary1);
        when(gymMapper.toTrainingSummary(training2, trainee1, trainer2)).thenReturn(trainingSummary2);

        List<TrainingSummary> result = trainingService.getTraineeTrainingList(request);
        assertEquals(mappedTrainings, result);
        verify(authComponent, times(1)).authenticate(request.credentials());
        verify(trainingRepository, times(1)).findTraineeTrainingsByCriteria(
                request.credentials().username(),
                request.fromDate(),
                request.toDate(),
                request.trainerName(),
                request.trainingType().trainingTypeName());
        verify(gymMapper, times(1)).toTrainingSummary(training1, trainee1, trainer1);
        verify(gymMapper, times(1)).toTrainingSummary(training2, trainee1, trainer2);
    }
}
