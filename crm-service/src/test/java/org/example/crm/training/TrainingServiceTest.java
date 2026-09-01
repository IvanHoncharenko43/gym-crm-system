package org.example.crm.training;

import org.example.crm.TestUtils;
import org.example.crm.core.dto.ActionType;
import org.example.crm.exception.EntityNotFoundException;
import org.example.crm.core.service.GymMapper;
import org.example.crm.exception.InvalidRequestDataException;
import org.example.crm.monitoring.GymCrmMetrics;
import org.example.crm.trainee.controller.request.GetTraineeTrainingsRequest;
import org.example.crm.trainee.repository.TraineeEntity;
import org.example.crm.trainee.repository.TraineeRepository;
import org.example.crm.trainer.controller.request.GetTrainerTrainingsRequest;
import org.example.crm.trainer.messaging.TrainerWorkloadUpdateEvent;
import org.example.crm.trainer.repository.TrainerEntity;
import org.example.crm.trainer.repository.TrainerRepository;
import org.example.crm.trainer.messaging.TrainerWorkloadProducerService;
import org.example.crm.training.controller.request.CreateTrainingRequest;
import org.example.crm.training.controller.response.TrainingSummary;
import org.example.crm.trainingType.dto.TrainingType;
import org.example.crm.training.controller.response.Trainings;
import org.example.crm.training.repository.TrainingEntity;
import org.example.crm.training.repository.TrainingRepository;
import org.example.crm.training.service.TrainingService;
import org.example.crm.trainingType.repository.TrainingTypeEntity;
import org.example.crm.user.controller.dto.FullName;
import org.example.crm.user.repository.UserEntity;
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
    private static final String TRAINER_USERNAME = "John.Doe";
    private static final Long TRAINEE_ID = 3L;
    private static final String TRAINEE_USERNAME = "John.Doe1";

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private GymMapper gymMapper;

    @Mock
    private GymCrmMetrics gymCrmMetrics;

    @Mock
    private TrainerWorkloadProducerService trainerWorkloadProducerService;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void create_CreateAndReturnTrainingResponse_AllEntitiesValid() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TRAINER_USERNAME, TRAINEE_USERNAME, "Cardio",
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
        TrainingTypeEntity trainingType = new TrainingTypeEntity();
        trainingType.setTrainingTypeName(TrainingType.YOGA);
        training.setTrainingType(trainingType);
        TrainingSummary expectedResponse = new TrainingSummary(
                TRAINING_ID,
                TestUtils.getTrainerSummary(TRAINER_USERNAME),
                TestUtils.getTraineeSummary(TRAINEE_USERNAME),
                "Cardio",
                TrainingType.CARDIO,
                LocalDate.of(2026, 5, 12), 45
        );
        TrainerWorkloadUpdateEvent workloadUpdateEvent = new TrainerWorkloadUpdateEvent(
                TRAINER_USERNAME, new FullName("John", "Doe"), true,
                LocalDate.of(2026, 5, 12), 45, ActionType.ADD
        );

        when(traineeRepository.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));
        when(gymMapper.toTraining(request, trainee, trainer)).thenReturn(training);
        when(trainingRepository.save(training)).thenReturn(training);
        when(gymMapper.toTrainerWorkloadUpdateEvent(trainer, training, ActionType.ADD)).thenReturn(workloadUpdateEvent);
        when(gymMapper.toTrainingSummary(training, trainee, trainer)).thenReturn(expectedResponse);

        trainingService.create(request);

        verify(traineeRepository, times(1)).findByUsername(TRAINEE_USERNAME);
        verify(trainerRepository, times(1)).findByUsername(TRAINER_USERNAME);
        verify(gymMapper, times(1)).toTraining(request, trainee, trainer);
        verify(trainingRepository, times(1)).save(training);
        verify(gymMapper, times(1)).toTrainerWorkloadUpdateEvent(trainer, training, ActionType.ADD);
        verify(trainerWorkloadProducerService, times(1)).publishTrainerWorkloadUpdateEvent(workloadUpdateEvent);
        verify(gymMapper, times(1)).toTrainingSummary(training, trainee, trainer);
    }

    @Test
    void create_ThrowInvalidRequestDataException_TraineeNotFound() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TRAINER_USERNAME, TRAINEE_USERNAME, "Cardio",
                LocalDate.of(2026, 5, 12), 45
        );

        when(traineeRepository.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.empty());

        InvalidRequestDataException exception = assertThrows(InvalidRequestDataException.class,
                () -> trainingService.create(request));
        assertTrue(exception.getMessage().contains("Trainee"));
        verify(traineeRepository, times(1)).findByUsername(TRAINEE_USERNAME);
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void create_ThrowInvalidRequestDataException_TraineeIsInactive() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TRAINER_USERNAME, TRAINEE_USERNAME, "Cardio",
                LocalDate.of(2026, 5, 12), 45
        );
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(new UserEntity());
        trainee.getUser().setIsActive(false);

        when(traineeRepository.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(trainee));

        InvalidRequestDataException exception = assertThrows(InvalidRequestDataException.class,
                () -> trainingService.create(request));
        assertTrue(exception.getMessage().contains("Trainee"));
        verify(traineeRepository, times(1)).findByUsername(TRAINEE_USERNAME);
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void create_ThrowInvalidRequestDataException_TrainerNotFound() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TRAINER_USERNAME, TRAINEE_USERNAME, "Cardio",
                LocalDate.of(2026, 5, 12), 45
        );
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(new UserEntity());
        trainee.getUser().setIsActive(true);

        when(traineeRepository.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.empty());

        InvalidRequestDataException exception = assertThrows(InvalidRequestDataException.class,
                () -> trainingService.create(request));
        assertTrue(exception.getMessage().contains("Trainer"));
        verify(traineeRepository, times(1)).findByUsername(TRAINEE_USERNAME);
        verify(trainerRepository, times(1)).findByUsername(TRAINER_USERNAME);
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void create_ThrowInvalidRequestDataException_TrainerIsInactive() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TRAINER_USERNAME, TRAINEE_USERNAME, "Cardio",
                LocalDate.of(2026, 5, 12), 45
        );
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(new UserEntity());
        trainee.getUser().setIsActive(true);
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUser(new UserEntity());
        trainer.getUser().setIsActive(false);

        when(traineeRepository.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));

        InvalidRequestDataException exception = assertThrows(InvalidRequestDataException.class,
                () -> trainingService.create(request));
        assertTrue(exception.getMessage().contains("Trainer"));
        verify(traineeRepository, times(1)).findByUsername(TRAINEE_USERNAME);
        verify(trainerRepository, times(1)).findByUsername(TRAINER_USERNAME);
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void cancel_DeleteTrainingAndNotifyWorkloadService_TrainingExistsAndDateIsInFuture() {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(TRAINER_ID);
        TrainingEntity training = new TrainingEntity();
        training.setId(TRAINING_ID);
        training.setTrainer(trainer);
        training.setTrainingDate(LocalDate.now().plusDays(7));
        TrainerWorkloadUpdateEvent workloadUpdateEvent = new TrainerWorkloadUpdateEvent(
                TRAINER_USERNAME, new FullName("John", "Doe"), true,
                training.getTrainingDate(), 45, ActionType.DELETE
        );

        when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.of(training));
        when(gymMapper.toTrainerWorkloadUpdateEvent(trainer, training, ActionType.DELETE)).thenReturn(workloadUpdateEvent);

        trainingService.cancel(TRAINING_ID);
        verify(trainingRepository, times(1)).findById(TRAINING_ID);
        verify(gymMapper, times(1)).toTrainerWorkloadUpdateEvent(trainer, training, ActionType.DELETE);
        verify(trainingRepository, times(1)).delete(training);
        verify(trainerWorkloadProducerService, times(1)).publishTrainerWorkloadUpdateEvent(workloadUpdateEvent);
    }

    @Test
    void cancel_ThrowEntityNotFoundException_TrainingDoesNotExist() {
        when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> trainingService.cancel(TRAINING_ID));
        assertTrue(exception.getMessage().contains("Training"));
        verify(trainingRepository, times(1)).findById(TRAINING_ID);
        verify(trainingRepository, never()).delete(any());
        verify(trainerWorkloadProducerService, never()).publishTrainerWorkloadUpdateEvent(any());
    }

    @Test
    void cancel_ThrowInvalidRequestDataException_TrainingDateHasAlreadyPassed() {
        TrainingEntity training = new TrainingEntity();
        training.setId(TRAINING_ID);
        training.setTrainingDate(LocalDate.now().minusDays(1));

        when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.of(training));

        InvalidRequestDataException exception = assertThrows(InvalidRequestDataException.class,
                () -> trainingService.cancel(TRAINING_ID));
        assertTrue(exception.getMessage().contains("already passed"));
        verify(trainingRepository, times(1)).findById(TRAINING_ID);
        verify(trainingRepository, never()).delete(any());
        verify(trainerWorkloadProducerService, never()).publishTrainerWorkloadUpdateEvent(any());
    }

    @Test
    void getById_ReturnResponse_TrainingAndRelationsExist() {
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
                TrainingType.CARDIO,
                LocalDate.of(2026, 5, 12), 45
        );

        when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.of(training));
        when(traineeRepository.findById(TRAINEE_ID)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findById(TRAINER_ID)).thenReturn(Optional.of(trainer));
        when(gymMapper.toTrainingSummary(training, trainee, trainer)).thenReturn(expectedResponse);

        TrainingSummary actualResponse = trainingService.getById(TRAINING_ID);
        assertEquals(expectedResponse, actualResponse);
        verify(trainingRepository, times(1)).findById(TRAINING_ID);
        verify(traineeRepository, times(1)).findById(TRAINEE_ID);
        verify(trainerRepository, times(1)).findById(TRAINER_ID);
        verify(gymMapper, times(1)).toTrainingSummary(training, trainee, trainer);
    }

    @Test
    void getById_ThrowEntityNotFoundException_TrainingIsMissing() {
        when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> trainingService.getById(TRAINING_ID));

        assertTrue(exception.getMessage().contains("Training"));
        verify(trainingRepository, times(1)).findById(TRAINING_ID);
        verify(gymMapper, never()).toTrainingSummary(any(), any(), any());
    }

    @Test
    void getTraineeTrainingList_ReturnTrainingsList_RequestIsValid(){
        GetTraineeTrainingsRequest request = new GetTraineeTrainingsRequest(
                TRAINEE_USERNAME, LocalDate.of(2023, 12, 3),
                LocalDate.of(2026, 5, 12), "Doe", TrainingType.CARDIO);
        TraineeEntity trainee1 = new TraineeEntity();
        trainee1.setId(TRAINEE_ID);
        TrainerEntity trainer1 = new TrainerEntity();
        trainer1.setId(TRAINER_ID);
        TrainerEntity trainer2 = new TrainerEntity();
        trainer2.setId(TRAINER_ID+1);
        TrainingEntity training1 = new TrainingEntity();
        training1.setId(TRAINING_ID);
        training1.setTrainee(trainee1);
        training1.setTrainer(trainer1);
        TrainingEntity training2 = new TrainingEntity();
        training2.setId(TRAINING_ID+1);
        training2.setTrainee(trainee1);
        training2.setTrainer(trainer2);
        List<TrainingEntity> trainings = List.of(training1, training2);
        TrainingSummary trainingSummary1 = new TrainingSummary(
                TRAINING_ID,
                TestUtils.getTrainerSummary(TRAINER_ID),
                TestUtils.getTraineeSummary(TRAINEE_ID),
                "Cardio",
                TrainingType.CARDIO,
                LocalDate.of(2024, 5, 12), 45
        );
        TrainingSummary trainingSummary2 = new TrainingSummary(
                TRAINING_ID+1,
                TestUtils.getTrainerSummary(TRAINER_ID+1),
                TestUtils.getTraineeSummary(TRAINEE_ID),
                "Cardio",
                TrainingType.CARDIO,
                LocalDate.of(2026, 5, 12), 45
        );
        List<TrainingSummary> mappedTrainings = List.of(trainingSummary1, trainingSummary2);

        when(trainingRepository.findTraineeTrainings(request.username(), request.fromDate(),
                request.toDate(), request.trainerName(), request.trainingType().name()))
                .thenReturn(trainings);
        when(gymMapper.toTrainingSummary(training1, trainee1, trainer1)).thenReturn(trainingSummary1);
        when(gymMapper.toTrainingSummary(training2, trainee1, trainer2)).thenReturn(trainingSummary2);

        Trainings result = trainingService.getTraineeTrainingList(request);
        assertEquals(mappedTrainings, result.trainings());
        verify(trainingRepository, times(1)).findTraineeTrainings(
                request.username(), request.fromDate(), request.toDate(),
                request.trainerName(), request.trainingType().name());
        verify(gymMapper, times(1)).toTrainingSummary(training1, trainee1, trainer1);
        verify(gymMapper, times(1)).toTrainingSummary(training2, trainee1, trainer2);
    }

    @Test
    void getTrainerTrainingList_ReturnTrainingsList_RequestIsValid(){
        GetTrainerTrainingsRequest request = new GetTrainerTrainingsRequest(
                TRAINER_USERNAME,
                LocalDate.of(2023, 12, 3),
                LocalDate.of(2026, 5, 12),
                "Doe");
        TrainerEntity trainer1 = new TrainerEntity();
        trainer1.setId(TRAINER_ID);
        TraineeEntity trainee1 = new TraineeEntity();
        trainee1.setId(TRAINEE_ID);
        TraineeEntity trainee2 = new TraineeEntity();
        trainee2.setId(TRAINEE_ID+1);
        TrainingEntity training1 = new TrainingEntity();
        training1.setId(TRAINING_ID);
        training1.setTrainee(trainee1);
        training1.setTrainer(trainer1);
        TrainingEntity training2 = new TrainingEntity();
        training2.setId(TRAINING_ID+1);
        training2.setTrainee(trainee2);
        training2.setTrainer(trainer1);
        List<TrainingEntity> trainings = List.of(training1, training2);
        TrainingSummary trainingSummary1 = new TrainingSummary(
                TRAINING_ID,
                TestUtils.getTrainerSummary(TRAINER_ID),
                TestUtils.getTraineeSummary(TRAINEE_ID),
                "Cardio",
                TrainingType.CARDIO,
                LocalDate.of(2024, 5, 12), 45
        );
        TrainingSummary trainingSummary2 = new TrainingSummary(
                TRAINING_ID+1,
                TestUtils.getTrainerSummary(TRAINER_ID),
                TestUtils.getTraineeSummary(TRAINEE_ID+1),
                "Cardio",
                TrainingType.CARDIO,
                LocalDate.of(2026, 5, 12), 45
        );
        List<TrainingSummary> mappedTrainings = List.of(trainingSummary1, trainingSummary2);

        when(trainingRepository.findTrainerTrainings(request.username(), request.fromDate(),
                request.toDate(), request.traineeName()))
                .thenReturn(trainings);
        when(gymMapper.toTrainingSummary(training1, trainee1, trainer1)).thenReturn(trainingSummary1);
        when(gymMapper.toTrainingSummary(training2, trainee2, trainer1)).thenReturn(trainingSummary2);

        Trainings result = trainingService.getTrainerTrainingList(request);
        assertEquals(mappedTrainings, result.trainings());
        verify(trainingRepository, times(1)).findTrainerTrainings(
                request.username(), request.fromDate(), request.toDate(), request.traineeName());
        verify(gymMapper, times(1)).toTrainingSummary(training1, trainee1, trainer1);
        verify(gymMapper, times(1)).toTrainingSummary(training2, trainee2, trainer1);
    }
}
