package org.example.trainer;

import org.example.TestUtils;
import org.example.security.service.OwnershipVerifier;
import org.example.exception.EntityNotFoundException;
import org.example.exception.InvalidRequestDataException;
import org.example.monitoring.GymCrmMetrics;
import org.example.trainer.controller.request.CreateTrainerRequest;
import org.example.trainer.controller.response.TrainerSummary;
import org.example.trainer.controller.request.UpdateTrainerRequest;
import org.example.trainer.controller.response.Trainers;
import org.example.trainer.repository.TrainerEntity;
import org.example.trainer.repository.TrainerRepository;
import org.example.trainer.service.TrainerService;
import org.example.trainingType.dto.TrainingType;
import org.example.trainingType.repository.TrainingTypeEntity;
import org.example.trainingType.repository.TrainingTypeRepository;
import org.example.user.controller.dto.FullName;
import org.example.user.controller.dto.UserProfile;
import org.example.core.service.GymMapper;
import org.example.user.repository.UserEntity;
import org.example.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainerServiceTest {

    private static final Long TRAINER_ID = 1L;
    private static final String USERNAME = "John.Doe";
    private static final UserDetails USER_DETAILS = TestUtils.getTrainerUserDetails();

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GymMapper gymMapper;

    @Mock
    private OwnershipVerifier ownershipVerifier;

    @Mock
    private GymCrmMetrics gymCrmMetrics;

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void create_CreateAndReturnTrainerResponse_RequestIsValid() {
        CreateTrainerRequest request = new CreateTrainerRequest(
                new FullName("John", "Doe"), TrainingType.YOGA
        );
        TrainingTypeEntity trainingType = new TrainingTypeEntity();
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUser(new UserEntity());
        trainer.setId(TRAINER_ID);
        TrainerSummary expectedResponse = new TrainerSummary(5L, new UserProfile("John.Doe22"), TrainingType.YOGA);

        when(trainingTypeRepository.findByTrainingTypeName(request.specialization()))
                .thenReturn(Optional.of(trainingType));
        when(userRepository.findUsernamesByBaseNameForUpdate(anyString())).thenReturn(Collections.emptyList());
        when(gymMapper.toTrainerEntity(eq(request), eq(trainingType), anySet())).thenReturn(trainer);
        when(trainerRepository.save(trainer)).thenReturn(trainer);
        when(gymMapper.toTrainerSummary(trainer)).thenReturn(expectedResponse);

        TrainerSummary actualResponse = trainerService.create(request);

        assertEquals(expectedResponse, actualResponse);
        verify(trainingTypeRepository, times(1)).findByTrainingTypeName(
                request.specialization()
        );
        verify(userRepository, times(1)).findUsernamesByBaseNameForUpdate(anyString());
        verify(gymMapper, times(1)).toTrainerEntity(eq(request), eq(trainingType), anySet());
        verify(trainerRepository, times(1)).save(trainer);
        verify(gymMapper, times(1)).toTrainerSummary(trainer);
    }

    @Test
    void create_ThrowInvalidRequestDataException_TrainingTypeDoesNotExist() {
        CreateTrainerRequest request = new CreateTrainerRequest(
                new FullName("John", "Doe"), TrainingType.YOGA
        );

        when(trainingTypeRepository.findByTrainingTypeName(request.specialization()))
                .thenReturn(Optional.empty());

        InvalidRequestDataException exception = assertThrows(InvalidRequestDataException.class,
                () -> trainerService.create(request));
        assertTrue(exception.getMessage().contains("not found"));
        verify(trainingTypeRepository, times(1)).findByTrainingTypeName(
                request.specialization()
        );
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void update_UpdateAndReturnResponse_TrainerExists() {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                new FullName("John", "Doe"), TrainingType.YOGA
        );
        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(TRAINER_ID);
        trainer.setUser(new UserEntity());
        trainer.getUser().setUsername(USERNAME);
        TrainingTypeEntity trainingType = new TrainingTypeEntity();
        TrainerSummary expectedResponse = new TrainerSummary(
                TRAINER_ID, new UserProfile(USERNAME),
                TrainingType.YOGA
        );

        when(trainerRepository.findById(TRAINER_ID)).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByTrainingTypeName(request.specialization()))
                .thenReturn(Optional.of(trainingType));
        when(gymMapper.toTrainerEntity(request, trainer, trainingType))
                .thenReturn(trainer);
        when(trainerRepository.save(trainer)).thenReturn(trainer);
        when(gymMapper.toTrainerSummary(trainer)).thenReturn(expectedResponse);

        TrainerSummary actualResponse = trainerService.update(TRAINER_ID, request, USER_DETAILS);

        assertEquals(expectedResponse, actualResponse);
        verify(ownershipVerifier, times(1)).verifyOwnershipByUsername(USERNAME, USER_DETAILS.getUsername());
        verify(trainerRepository, times(1)).findById(TRAINER_ID);
        verify(trainingTypeRepository, times(1)).findByTrainingTypeName(
                request.specialization()
        );
        verify(gymMapper, times(1)).toTrainerEntity(request, trainer, trainingType);
        verify(trainerRepository, times(1)).save(trainer);
        verify(gymMapper, times(1)).toTrainerSummary(trainer);
    }

    @Test
    void update_ThrowEntityNotFoundException_TrainerDoesNotExist() {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                new FullName("John", "Doe"), TrainingType.YOGA
        );

        when(trainerRepository.findById(TRAINER_ID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> trainerService.update(TRAINER_ID, request, USER_DETAILS));
        assertTrue(exception.getMessage().contains("not found"));
        verify(trainerRepository, times(1)).findById(TRAINER_ID);
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void update_ThrowInvalidRequestDataException_TrainingTypeDoesNotExist() {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                new FullName("John", "Doe"), TrainingType.YOGA
        );
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUser(new UserEntity());
        trainer.getUser().setUsername(USERNAME);

        when(trainerRepository.findById(TRAINER_ID)).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByTrainingTypeName(request.specialization()))
                .thenReturn(Optional.empty());

        InvalidRequestDataException exception = assertThrows(InvalidRequestDataException.class,
                () -> trainerService.update(TRAINER_ID, request, USER_DETAILS));
        assertTrue(exception.getMessage().contains("not found"));
        verify(ownershipVerifier, times(1)).verifyOwnershipByUsername(USERNAME, USER_DETAILS.getUsername());
        verify(trainerRepository, times(1)).findById(TRAINER_ID);
        verify(trainingTypeRepository, times(1)).findByTrainingTypeName(
                request.specialization()
        );
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void getById_ReturnResponse_TrainerExists() {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUser(new UserEntity());
        trainer.getUser().setIsActive(true);
        TrainerSummary expectedResponse = new TrainerSummary(
                TRAINER_ID, new UserProfile(USERNAME),
                TrainingType.YOGA
        );

        when(trainerRepository.findById(TRAINER_ID)).thenReturn(Optional.of(trainer));
        when(gymMapper.toTrainerSummary(trainer)).thenReturn(expectedResponse);

        TrainerSummary actualResponse = trainerService.getById(TRAINER_ID);
        assertEquals(expectedResponse, actualResponse);
        verify(trainerRepository, times(1)).findById(TRAINER_ID);
        verify(gymMapper, times(1)).toTrainerSummary(trainer);
    }

    @Test
    void getById_ThrowEntityNotFoundException_TrainerDoesNotExist() {
        when(trainerRepository.findById(TRAINER_ID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> trainerService.getById(TRAINER_ID));
        assertTrue(exception.getMessage().contains("not found"));
        verify(trainerRepository, times(1)).findById(TRAINER_ID);
    }

    @Test
    void getById_ThrowEntityNotFoundException_TrainerInactive() {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUser(new UserEntity());
        trainer.getUser().setIsActive(false);

        when(trainerRepository.findById(TRAINER_ID)).thenReturn(Optional.of(trainer));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> trainerService.getById(TRAINER_ID));
        assertTrue(exception.getMessage().contains("inactive"));
        verify(trainerRepository, times(1)).findById(TRAINER_ID);
    }

    @Test
    void getByUsername_ReturnResponse_TrainerExists() {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUser(new UserEntity());
        trainer.getUser().setIsActive(true);
        TrainerSummary expectedResponse = new TrainerSummary(
                TRAINER_ID, new UserProfile(USERNAME),
                TrainingType.YOGA
        );

        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(gymMapper.toTrainerSummary(trainer)).thenReturn(expectedResponse);

        TrainerSummary actualResponse = trainerService.getByUsername(USERNAME);
        assertEquals(expectedResponse, actualResponse);
        verify(trainerRepository, times(1)).findByUsername(USERNAME);
        verify(gymMapper, times(1)).toTrainerSummary(trainer);
    }

    @Test
    void getByUsername_ThrowEntityNotFoundException_TrainerDoesNotExist() {
        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> trainerService.getByUsername(USERNAME));
        assertTrue(exception.getMessage().contains("not found"));
        verify(trainerRepository, times(1)).findByUsername(USERNAME);
    }

    @Test
    void getByUsername_ThrowEntityNotFoundException_TrainerInactive() {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUser(new UserEntity());
        trainer.getUser().setIsActive(false);

        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> trainerService.getByUsername(USERNAME));
        assertTrue(exception.getMessage().contains("inactive"));
        verify(trainerRepository, times(1)).findByUsername(USERNAME);
    }

    @Test
    void changeActivity_ChangeToInactive_RequestIsValid(){
        TrainerEntity existingTrainer = new TrainerEntity();
        existingTrainer.setUser(new UserEntity());
        existingTrainer.getUser().setIsActive(true);
        existingTrainer.getUser().setUsername(USERNAME);

        when(trainerRepository.findById(TRAINER_ID)).thenReturn(Optional.of(existingTrainer));

        trainerService.changeActivity(TRAINER_ID, USER_DETAILS);
        verify(ownershipVerifier, times(1)).verifyOwnershipByUsername(USERNAME, USER_DETAILS.getUsername());
        verify(trainerRepository, times(1)).findById(TRAINER_ID);
        verify(trainerRepository, times(1)).save(existingTrainer);
    }

    @Test
    void changeActivity_ThrowEntityNotFoundException_TrainerDoesNotExist(){
        when(trainerRepository.findById(TRAINER_ID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> trainerService.changeActivity(TRAINER_ID, USER_DETAILS));
        assertTrue(exception.getMessage().contains("not found"));
        verify(trainerRepository, times(1)).findById(TRAINER_ID);
    }

    @Test
    void getUnassignedTrainersByTraineeList_ReturnTrainersList_RequestIsValid(){
        String traineeUsername = "John.Doe1";
        TrainerEntity trainer1 = new TrainerEntity();
        trainer1.setId(1L);
        trainer1.setUser(new UserEntity());
        trainer1.getUser().setIsActive(true);
        TrainerEntity trainer2 = new TrainerEntity();
        trainer2.setId(2L);
        trainer2.setUser(new UserEntity());
        trainer2.getUser().setIsActive(true);
        List<TrainerEntity> trainers = List.of(trainer1, trainer2);
        TrainerSummary trainerSummary1 = TestUtils.getTrainerSummary(1L);
        TrainerSummary trainerSummary2 = TestUtils.getTrainerSummary(2L);
        List<TrainerSummary> mappedTrainers = List.of(trainerSummary1, trainerSummary2);

        when(trainerRepository.findUnassignedTrainersByTraineeUsername(traineeUsername)).thenReturn(trainers);
        when(gymMapper.toTrainerSummary(trainer1)).thenReturn(trainerSummary1);
        when(gymMapper.toTrainerSummary(trainer2)).thenReturn(trainerSummary2);

        Trainers result = trainerService.getUnassignedTrainersByTraineeList(traineeUsername);
        assertEquals(mappedTrainers, result.trainers());
        verify(trainerRepository, times(1))
                .findUnassignedTrainersByTraineeUsername(traineeUsername);
        verify(gymMapper, times(1)).toTrainerSummary(trainer1);
        verify(gymMapper, times(1)).toTrainerSummary(trainer2);
    }
}
