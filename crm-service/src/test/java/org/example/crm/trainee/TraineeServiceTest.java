package org.example.crm.trainee;

import org.example.crm.TestUtils;
import org.example.crm.core.dto.ActionType;
import org.example.crm.exception.EntityNotFoundException;
import org.example.crm.monitoring.GymCrmMetrics;
import org.example.crm.trainee.controller.request.UpdateTraineeTrainersRequest;
import org.example.crm.trainer.controller.request.TrainerWorkloadRequest;
import org.example.crm.trainer.controller.response.TrainerSummary;
import org.example.crm.trainer.controller.response.Trainers;
import org.example.crm.trainer.repository.TrainerEntity;
import org.example.crm.trainer.repository.TrainerRepository;
import org.example.crm.trainer.service.TrainerWorkloadAdapter;
import org.example.crm.training.repository.TrainingEntity;
import org.example.crm.training.repository.TrainingRepository;
import org.example.crm.user.controller.dto.FullName;
import org.example.crm.core.service.GymMapper;
import org.example.crm.user.controller.dto.UserProfile;
import org.example.crm.trainee.controller.request.CreateTraineeRequest;
import org.example.crm.trainee.controller.response.TraineeSummary;
import org.example.crm.trainee.controller.request.UpdateTraineeRequest;
import org.example.crm.trainee.repository.TraineeEntity;
import org.example.crm.trainee.repository.TraineeRepository;
import org.example.crm.trainee.service.TraineeService;
import org.example.crm.user.repository.UserEntity;
import org.example.crm.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TraineeServiceTest {

    private static final Long TRAINEE_ID = 1L;
    private static final String USERNAME = "John.Doe";

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GymMapper gymMapper;

    @Mock
    private GymCrmMetrics gymCrmMetrics;

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TrainerWorkloadAdapter trainerWorkloadAdapter;

    @InjectMocks
    private TraineeService traineeService;

    @Test
    void create_CreateAndReturnTraineeResponse_RequestIsValid() {
        CreateTraineeRequest request = new CreateTraineeRequest(
                new FullName("John", "Doe"), LocalDate.of(2007, 3, 25), "Home 21 Street"
        );
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(new UserEntity());
        trainee.setId(TRAINEE_ID);
        trainee.getUser().setUsername(USERNAME);
        TraineeSummary expectedResponse = new TraineeSummary(TRAINEE_ID, new UserProfile(USERNAME),
                LocalDate.of(2007, 3, 25), "Home 21 Street");

        when(userRepository.findUsernamesByBaseNameForUpdate(anyString())).thenReturn(Collections.emptyList());
        when(gymMapper.toTraineeEntity(eq(request), anySet())).thenReturn(trainee);
        when(traineeRepository.save(trainee)).thenReturn(trainee);
        when(gymMapper.toTraineeSummary(trainee)).thenReturn(expectedResponse);

        TraineeSummary actualResponse = traineeService.create(request);

        assertEquals(expectedResponse, actualResponse);
        verify(userRepository, times(1)).findUsernamesByBaseNameForUpdate(anyString());
        verify(gymMapper, times(1)).toTraineeEntity(eq(request), anySet());
        verify(traineeRepository, times(1)).save(trainee);
        verify(gymMapper, times(1)).toTraineeSummary(trainee);
    }

    @Test
    void update_UpdateAndReturnResponse_TraineeExists() {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                new FullName("John", "Doe"),
                LocalDate.of(2007, 3, 25), "Home 21 Street"
        );
        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(TRAINEE_ID);
        trainee.setUser(new UserEntity());
        trainee.getUser().setUsername(USERNAME);
        TraineeSummary expectedResponse = new TraineeSummary(
                TRAINEE_ID, new UserProfile(USERNAME),
                LocalDate.of(2007, 3, 25), "Home 21 Street"
        );

        when(traineeRepository.findById(TRAINEE_ID)).thenReturn(Optional.of(trainee));
        when(gymMapper.toTraineeEntity(request, trainee))
                .thenReturn(trainee);
        when(traineeRepository.save(trainee)).thenReturn(trainee);
        when(gymMapper.toTraineeSummary(trainee)).thenReturn(expectedResponse);

        TraineeSummary actualResponse = traineeService.update(TRAINEE_ID, request);

        assertEquals(expectedResponse, actualResponse);
        verify(traineeRepository, times(1)).findById(TRAINEE_ID);
        verify(gymMapper, times(1)).toTraineeEntity(request, trainee);
        verify(traineeRepository, times(1)).save(trainee);
        verify(gymMapper, times(1)).toTraineeSummary(trainee);
    }

    @Test
    void update_ThrowEntityNotFoundException_TraineeDoesNotExist() {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                new FullName("John", "Doe"),
                LocalDate.of(2007, 3, 25), "Home 21 Street");

        when(traineeRepository.findById(TRAINEE_ID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> traineeService.update(TRAINEE_ID, request));
        assertTrue(exception.getMessage().contains("not found"));
        verify(traineeRepository, times(1)).findById(TRAINEE_ID);
        verify(traineeRepository, never()).save(any());
    }

    @Test
    void getById_ReturnResponse_TraineeExists() {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(new UserEntity());
        trainee.getUser().setIsActive(true);
        TraineeSummary expectedResponse = new TraineeSummary(
                TRAINEE_ID, new UserProfile(USERNAME),
                LocalDate.of(2007, 3, 25), "Home 21 Street"
        );

        when(traineeRepository.findById(TRAINEE_ID)).thenReturn(Optional.of(trainee));
        when(gymMapper.toTraineeSummary(trainee)).thenReturn(expectedResponse);

        TraineeSummary actualResponse = traineeService.getById(TRAINEE_ID);
        assertEquals(expectedResponse, actualResponse);
        verify(traineeRepository, times(1)).findById(TRAINEE_ID);
        verify(gymMapper, times(1)).toTraineeSummary(trainee);
    }

    @Test
    void getById_ThrowEntityNotFoundException_TraineeDoesNotExist() {
        when(traineeRepository.findById(TRAINEE_ID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> traineeService.getById(TRAINEE_ID));
        assertTrue(exception.getMessage().contains("not found"));
        verify(traineeRepository, times(1)).findById(TRAINEE_ID);
    }

    @Test
    void getById_ThrowEntityNotFoundException_TraineeInactive() {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(new UserEntity());
        trainee.getUser().setIsActive(false);

        when(traineeRepository.findById(TRAINEE_ID)).thenReturn(Optional.of(trainee));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> traineeService.getById(TRAINEE_ID));
        assertTrue(exception.getMessage().contains("inactive"));
        verify(traineeRepository, times(1)).findById(TRAINEE_ID);
    }

    @Test
    void getByUsername_ReturnResponse_TraineeExists() {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(new UserEntity());
        trainee.getUser().setIsActive(true);
        TraineeSummary expectedResponse = new TraineeSummary(
                TRAINEE_ID, new UserProfile(USERNAME),
                LocalDate.of(2007, 3, 25), "Home 21 Street"
        );

        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(gymMapper.toTraineeSummary(trainee)).thenReturn(expectedResponse);

        TraineeSummary actualResponse = traineeService.getByUsername(USERNAME);
        assertEquals(expectedResponse, actualResponse);
        verify(traineeRepository, times(1)).findByUsername(USERNAME);
        verify(gymMapper, times(1)).toTraineeSummary(trainee);
    }

    @Test
    void getByUsername_ThrowEntityNotFoundException_TraineeDoesNotExist() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> traineeService.getByUsername(USERNAME));
        assertTrue(exception.getMessage().contains("not found"));
        verify(traineeRepository, times(1)).findByUsername(USERNAME);
    }

    @Test
    void getByUsername_ThrowEntityNotFoundException_TraineeInactive() {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(new UserEntity());
        trainee.getUser().setIsActive(false);

        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> traineeService.getByUsername(USERNAME));
        assertTrue(exception.getMessage().contains("inactive"));
        verify(traineeRepository, times(1)).findByUsername(USERNAME);
    }

    @Test
    void deleteByUsername_DeleteAndNotifyWorkloadService_TraineeHasTrainings() {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(new UserEntity());
        trainee.getUser().setUsername(USERNAME);
        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(1L);
        TrainingEntity training1 = new TrainingEntity();
        training1.setId(1L);
        training1.setTrainer(trainer);
        TrainingEntity training2 = new TrainingEntity();
        training2.setId(2L);
        training2.setTrainer(trainer);
        List<TrainingEntity> trainings = List.of(training1, training2);
        TrainerWorkloadRequest workloadRequest1 = new TrainerWorkloadRequest(
                "Trainer.Doe", new FullName("Jane", "Smith"), true,
                LocalDate.of(2026, 5, 12), 45, ActionType.DELETE
        );
        TrainerWorkloadRequest workloadRequest2 = new TrainerWorkloadRequest(
                "Trainer.Doe", new FullName("Jane", "Smith"), true,
                LocalDate.of(2026, 6, 1), 60, ActionType.DELETE
        );

        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(trainingRepository.findAllByTraineeUserUsername(USERNAME)).thenReturn(trainings);
        when(gymMapper.toTrainerWorkloadRequest(trainer, training1, ActionType.DELETE)).thenReturn(workloadRequest1);
        when(gymMapper.toTrainerWorkloadRequest(trainer, training2, ActionType.DELETE)).thenReturn(workloadRequest2);

        traineeService.deleteByUsername(USERNAME);

        verify(traineeRepository, times(1)).findByUsername(USERNAME);
        verify(trainingRepository, times(1)).findAllByTraineeUserUsername(USERNAME);
        verify(gymMapper, times(1)).toTrainerWorkloadRequest(trainer, training1, ActionType.DELETE);
        verify(gymMapper, times(1)).toTrainerWorkloadRequest(trainer, training2, ActionType.DELETE);
        verify(trainerWorkloadAdapter, times(1)).updateTrainerWorkload(workloadRequest1);
        verify(trainerWorkloadAdapter, times(1)).updateTrainerWorkload(workloadRequest2);
        verify(traineeRepository, times(1)).deleteByUserUsername(USERNAME);
    }

    @Test
    void deleteByUsername_DeleteWithoutNotifyingWorkloadService_TraineeHasNoTrainings() {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(new UserEntity());
        trainee.getUser().setUsername(USERNAME);

        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(trainingRepository.findAllByTraineeUserUsername(USERNAME)).thenReturn(List.of());

        traineeService.deleteByUsername(USERNAME);
        verify(traineeRepository, times(1)).findByUsername(USERNAME);
        verify(trainingRepository, times(1)).findAllByTraineeUserUsername(USERNAME);
        verify(trainerWorkloadAdapter, never()).updateTrainerWorkload(any());
        verify(traineeRepository, times(1)).deleteByUserUsername(USERNAME);
    }

    @Test
    void deleteByUsername_DoNothing_TraineeDoesNotExist() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        traineeService.deleteByUsername(USERNAME);

        verify(traineeRepository, times(1)).findByUsername(USERNAME);
        verify(trainingRepository, never()).findAllByTraineeUserUsername(anyString());
        verify(trainerWorkloadAdapter, never()).updateTrainerWorkload(any());
        verify(traineeRepository, never()).deleteByUserUsername(anyString());
    }

    @Test
    void changeActivity_ChangeToInactive_RequestIsValid(){
        TraineeEntity existingTrainee = new TraineeEntity();
        existingTrainee.setUser(new UserEntity());
        existingTrainee.getUser().setIsActive(true);
        existingTrainee.getUser().setUsername(USERNAME);

        when(traineeRepository.findById(TRAINEE_ID)).thenReturn(Optional.of(existingTrainee));

        traineeService.changeActivity(TRAINEE_ID);
        verify(traineeRepository, times(1)).findById(TRAINEE_ID);
        verify(traineeRepository, times(1)).save(existingTrainee);
    }

    @Test
    void changeActivity_ThrowEntityNotFoundException_TraineeDoesNotExist(){
        when(traineeRepository.findById(TRAINEE_ID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> traineeService.changeActivity(TRAINEE_ID));
        assertTrue(exception.getMessage().contains("not found"));
        verify(traineeRepository, times(1)).findById(TRAINEE_ID);
    }

    @Test
    void updateTrainersList_UpdateList_RequestIsValid(){
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(new UserEntity());
        trainee.getUser().setIsActive(true);
        trainee.getUser().setUsername(USERNAME);

        TrainerEntity oldTrainer1 = new TrainerEntity();
        oldTrainer1.setUser(new UserEntity());
        oldTrainer1.getUser().setUsername("Old.Doe");
        oldTrainer1.setTrainees(new HashSet<>(Set.of(trainee)));

        TrainerEntity oldTrainer2 = new TrainerEntity();
        oldTrainer2.setUser(new UserEntity());
        oldTrainer2.getUser().setUsername("Old.Smith");
        oldTrainer2.setTrainees(new HashSet<>(Set.of(trainee)));

        trainee.setTrainers(new HashSet<>(Set.of(oldTrainer1, oldTrainer2)));
        List<String> newTrainersUsernames = List.of("New.Doe", "New.Smith");
        TrainerEntity newTrainer1 = new TrainerEntity();
        newTrainer1.setUser(new UserEntity());
        newTrainer1.getUser().setUsername("New.Doe");
        newTrainer1.setTrainees(new HashSet<>());

        TrainerEntity newTrainer2 = new TrainerEntity();
        newTrainer2.setUser(new UserEntity());
        newTrainer2.getUser().setUsername("New.Smith");
        newTrainer2.setTrainees(new HashSet<>());
        List<TrainerEntity> newTrainers = List.of(newTrainer1, newTrainer2);
        List<TrainerSummary> newTrainersSummary = List.of(TestUtils.getTrainerSummary(1L), TestUtils.getTrainerSummary(2L));

        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(newTrainersUsernames);

        when(traineeRepository.findById(TRAINEE_ID)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsernames(newTrainersUsernames)).thenReturn(newTrainers);
        when(gymMapper.toTrainerSummary(newTrainer1)).thenReturn(newTrainersSummary.get(0));
        when(gymMapper.toTrainerSummary(newTrainer2)).thenReturn(newTrainersSummary.get(1));

        Trainers result = traineeService.updateTrainersList(TRAINEE_ID, request);
        assertEquals(2, result.trainers().size());
        assertEquals(newTrainersSummary, result.trainers());
        verify(traineeRepository, times(1)).findById(TRAINEE_ID);
        verify(trainerRepository, times(1)).findByUsernames(newTrainersUsernames);
        verify(gymMapper, times(1)).toTrainerSummary(newTrainer1);
        verify(gymMapper, times(1)).toTrainerSummary(newTrainer2);
    }
}
