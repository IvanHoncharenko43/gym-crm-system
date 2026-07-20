package org.example.trainer;

import org.example.TestUtils;
import org.example.core.dto.ChangeActivityRequest;
import org.example.core.dto.ChangePasswordRequest;
import org.example.core.service.AuthenticationComponent;
import org.example.exception.EntityNotFoundException;
import org.example.exception.InvalidPasswordException;
import org.example.exception.InvalidRequestDataException;
import org.example.trainer.dto.CreateTrainerRequest;
import org.example.trainer.dto.TrainerSummary;
import org.example.trainer.dto.UnassignedTrainersRequest;
import org.example.trainer.dto.UpdateTrainerRequest;
import org.example.trainer.repository.TrainerEntity;
import org.example.trainer.repository.TrainerRepository;
import org.example.trainer.service.TrainerService;
import org.example.training.dto.TrainingType;
import org.example.core.repository.TrainingTypeEntity;
import org.example.core.repository.TrainingTypeRepository;
import org.example.user.dto.UserCredentials;
import org.example.user.dto.FullName;
import org.example.user.dto.UserProfile;
import org.example.core.service.GymMapper;
import org.example.user.repository.UserEntity;
import org.example.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainerServiceTest {

    private static final Long TRAINER_ID = 1L;
    private static final String USERNAME = "John.Doe";
    private static final String PASSWORD = "122333test";
    private static final UserCredentials CREDENTIALS = new UserCredentials(USERNAME, PASSWORD);

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GymMapper gymMapper;

    @Mock
    private AuthenticationComponent authenticator;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void create_CreateAndReturnTrainerResponse_RequestIsValid() {
        CreateTrainerRequest request = new CreateTrainerRequest(
                new FullName("John", "Doe"), TrainingType.YOGA
        );
        TrainingTypeEntity trainingType = new TrainingTypeEntity();
        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(TRAINER_ID);
        TrainerSummary expectedResponse = new TrainerSummary(
                TRAINER_ID, new UserProfile(USERNAME),
                TrainingType.YOGA
        );
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        when(trainingTypeRepository.findByName(request.specialization()))
                .thenReturn(Optional.of(trainingType));
        when(userRepository.findUsernamesByBaseName(anyString())).thenReturn(Collections.emptyList());
        when(gymMapper.toTrainerEntity(eq(request), eq(trainingType), anySet())).thenReturn(trainer);
        when(trainerRepository.save(trainer)).thenReturn(trainer);
        when(gymMapper.toTrainerSummary(trainer)).thenReturn(expectedResponse);

        TrainerSummary actualResponse = trainerService.create(request);

        assertEquals(expectedResponse, actualResponse);
        verify(trainingTypeRepository, times(1)).findByName(
                request.specialization()
        );
        verify(transactionTemplate, times(1)).execute(any());
        verify(userRepository, times(1)).findUsernamesByBaseName(anyString());
        verify(gymMapper, times(1)).toTrainerEntity(eq(request), eq(trainingType), anySet());
        verify(trainerRepository, times(1)).save(trainer);
        verify(gymMapper, times(1)).toTrainerSummary(trainer);
    }

    @Test
    void create_ThrowInvalidRequestDataException_TrainingTypeDoesNotExist() {
        CreateTrainerRequest request = new CreateTrainerRequest(
                new FullName("John", "Doe"), TrainingType.YOGA
        );

        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
        when(trainingTypeRepository.findByName(request.specialization()))
                .thenReturn(Optional.empty());

        InvalidRequestDataException exception = assertThrows(InvalidRequestDataException.class,
                () -> trainerService.create(request));
        assertTrue(exception.getMessage().contains("not found"));
        verify(transactionTemplate, times(1)).execute(any());
        verify(trainingTypeRepository, times(1)).findByName(
                request.specialization()
        );
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void update_UpdateAndReturnResponse_TrainerExists() {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                TRAINER_ID, CREDENTIALS, new FullName("John", "Doe"),
                TrainingType.YOGA
        );
        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(TRAINER_ID);
        TrainingTypeEntity trainingType = new TrainingTypeEntity();
        TrainerSummary expectedResponse = new TrainerSummary(
                TRAINER_ID, new UserProfile(USERNAME),
                TrainingType.YOGA
        );

        when(trainerRepository.findById(TRAINER_ID)).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByName(request.specialization()))
                .thenReturn(Optional.of(trainingType));
        when(gymMapper.toTrainerEntity(request, trainer, trainingType))
                .thenReturn(trainer);
        when(trainerRepository.save(trainer)).thenReturn(trainer);
        when(gymMapper.toTrainerSummary(trainer)).thenReturn(expectedResponse);

        TrainerSummary actualResponse = trainerService.update(request);

        assertEquals(expectedResponse, actualResponse);
        verify(authenticator, times(1)).authenticate(CREDENTIALS);
        verify(trainerRepository, times(1)).findById(TRAINER_ID);
        verify(trainingTypeRepository, times(1)).findByName(
                request.specialization()
        );
        verify(gymMapper, times(1)).toTrainerEntity(request, trainer, trainingType);
        verify(trainerRepository, times(1)).save(trainer);
        verify(gymMapper, times(1)).toTrainerSummary(trainer);
    }

    @Test
    void update_ThrowEntityNotFoundException_TrainerDoesNotExist() {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                TRAINER_ID, CREDENTIALS, new FullName("John", "Doe"),
                TrainingType.YOGA
        );

        when(trainerRepository.findById(TRAINER_ID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> trainerService.update(request));
        assertTrue(exception.getMessage().contains("not found"));
        verify(authenticator, times(1)).authenticate(CREDENTIALS);
        verify(trainerRepository, times(1)).findById(TRAINER_ID);
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void update_ThrowInvalidRequestDataException_TrainingTypeDoesNotExist() {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                TRAINER_ID, CREDENTIALS, new FullName("John", "Doe"),
                TrainingType.YOGA
        );
        TrainerEntity trainer = new TrainerEntity();

        when(trainerRepository.findById(TRAINER_ID)).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByName(request.specialization()))
                .thenReturn(Optional.empty());

        InvalidRequestDataException exception = assertThrows(InvalidRequestDataException.class,
                () -> trainerService.update(request));
        assertTrue(exception.getMessage().contains("not found"));
        verify(authenticator, times(1)).authenticate(CREDENTIALS);
        verify(trainerRepository, times(1)).findById(TRAINER_ID);
        verify(trainingTypeRepository, times(1)).findByName(
                request.specialization()
        );
        verify(trainerRepository, never()).save(any());
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

        TrainerSummary actualResponse = trainerService.getByUsername(CREDENTIALS);
        assertEquals(expectedResponse, actualResponse);
        verify(authenticator, times(1)).authenticate(CREDENTIALS);
        verify(trainerRepository, times(1)).findByUsername(USERNAME);
        verify(gymMapper, times(1)).toTrainerSummary(trainer);
    }

    @Test
    void getByUsername_ThrowEntityNotFoundException_TrainerDoesNotExist() {
        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> trainerService.getByUsername(CREDENTIALS));
        assertTrue(exception.getMessage().contains("not found"));
        verify(authenticator, times(1)).authenticate(CREDENTIALS);
        verify(trainerRepository, times(1)).findByUsername(USERNAME);
    }

    @Test
    void getByUsername_ThrowEntityNotFoundException_TrainerInactive() {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUser(new UserEntity());
        trainer.getUser().setIsActive(false);

        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> trainerService.getByUsername(CREDENTIALS));
        assertTrue(exception.getMessage().contains("inactive"));
        verify(authenticator, times(1)).authenticate(CREDENTIALS);
        verify(trainerRepository, times(1)).findByUsername(USERNAME);
    }

    @Test
    void changePassword_Change_RequestIsValid(){
        String newPassword = "New_Password";
        ChangePasswordRequest request = new ChangePasswordRequest(CREDENTIALS, newPassword);
        TrainerEntity existingTrainer = new TrainerEntity();
        existingTrainer.setUser(new UserEntity());
        existingTrainer.getUser().setPassword(PASSWORD);
        existingTrainer.getUser().setIsActive(true);

        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(existingTrainer));
        when(trainerRepository.save(existingTrainer)).thenReturn(existingTrainer);

        trainerService.changePassword(request);

        verify(authenticator, times(1)).authenticate(CREDENTIALS);
        verify(trainerRepository, times(1)).findByUsername(USERNAME);
        verify(trainerRepository, times(1)).save(existingTrainer);
    }

    @Test
    void changePassword_ThrowEntityNotFoundException_TrainerDoesNotExist(){
        String newPassword = "New_Password";
        ChangePasswordRequest request = new ChangePasswordRequest(CREDENTIALS, newPassword);

        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> trainerService.changePassword(request));
        assertTrue(exception.getMessage().contains("not found"));
        verify(authenticator, times(1)).authenticate(CREDENTIALS);
        verify(trainerRepository, times(1)).findByUsername(USERNAME);
    }

    @Test
    void changePassword_ThrowEntityNotFoundException_TrainerIsInactive(){
        String newPassword = "New_Password";
        ChangePasswordRequest request = new ChangePasswordRequest(CREDENTIALS, newPassword);
        TrainerEntity inactiveTrainer = new TrainerEntity();
        inactiveTrainer.setUser(new UserEntity());
        inactiveTrainer.getUser().setIsActive(false);

        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(inactiveTrainer));
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> trainerService.changePassword(request));
        assertTrue(exception.getMessage().contains("inactive"));
        verify(authenticator, times(1)).authenticate(CREDENTIALS);
        verify(trainerRepository, times(1)).findByUsername(USERNAME);
    }

    @Test
    void changePassword_ThrowInvalidPasswordException_PasswordLessThan10Chars(){
        String newPassword = "short";
        ChangePasswordRequest request = new ChangePasswordRequest(CREDENTIALS, newPassword);
        TrainerEntity existingTrainer = new TrainerEntity();
        existingTrainer.setUser(new UserEntity());
        existingTrainer.getUser().setPassword(PASSWORD);
        existingTrainer.getUser().setIsActive(true);

        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(existingTrainer));
        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class,
                () -> trainerService.changePassword(request));
        assertTrue(exception.getMessage().contains("Password should be"));
        verify(authenticator, times(1)).authenticate(CREDENTIALS);
        verify(trainerRepository, times(1)).findByUsername(USERNAME);
    }

    @Test
    void changeActivity_ChangeToInactive_RequestIsValid(){
        ChangeActivityRequest request = new ChangeActivityRequest(CREDENTIALS);
        TrainerEntity existingTrainer = new TrainerEntity();
        existingTrainer.setUser(new UserEntity());
        existingTrainer.getUser().setIsActive(true);

        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(existingTrainer));

        trainerService.changeActivity(request);
        verify(authenticator, times(1)).authenticate(CREDENTIALS);
        verify(trainerRepository, times(1)).save(existingTrainer);
    }

    @Test
    void changeActivity_ThrowEntityNotFoundException_TrainerDoesNotExist(){
        ChangeActivityRequest request = new ChangeActivityRequest(CREDENTIALS);

        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> trainerService.changeActivity(request));
        assertTrue(exception.getMessage().contains("not found"));
        verify(authenticator, times(1)).authenticate(CREDENTIALS);
        verify(trainerRepository, times(1)).findByUsername(USERNAME);
    }

    @Test
    void getUnassignedTrainersByTraineeList_ReturnTrainersList_RequestIsValid(){
        String traineeUsername = "John.Doe1";
        UnassignedTrainersRequest request = new UnassignedTrainersRequest(CREDENTIALS, traineeUsername);
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

        List<TrainerSummary> result = trainerService.getUnassignedTrainersByTraineeList(request);
        assertEquals(mappedTrainers, result);
        verify(authenticator, times(1)).authenticate(CREDENTIALS);
        verify(trainerRepository, times(1))
                .findUnassignedTrainersByTraineeUsername(traineeUsername);
        verify(gymMapper, times(1)).toTrainerSummary(trainer1);
        verify(gymMapper, times(1)).toTrainerSummary(trainer2);
    }
}
