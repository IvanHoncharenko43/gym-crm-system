package org.example.trainee;

import org.example.core.dto.ChangeActivityRequest;
import org.example.core.dto.ChangePasswordRequest;
import org.example.core.service.AuthenticationComponent;
import org.example.exception.EntityNotFoundException;
import org.example.exception.InvalidPasswordException;
import org.example.exception.InvalidStatusTransitionException;
import org.example.trainer.repository.TrainerRepository;
import org.example.user.dto.UserCredentials;
import org.example.user.dto.FullName;
import org.example.core.service.GymMapper;
import org.example.user.dto.UserProfile;
import org.example.trainee.dto.CreateTraineeRequest;
import org.example.trainee.dto.TraineeSummary;
import org.example.trainee.dto.UpdateTraineeRequest;
import org.example.trainee.repository.TraineeEntity;
import org.example.trainee.repository.TraineeRepository;
import org.example.trainee.service.TraineeService;
import org.example.user.repository.UserEntity;
import org.example.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TraineeServiceTest {

    private static final Long TRAINEE_ID = 1L;
    private static final String USERNAME = "John.Doe";
    private static final String PASSWORD = "122333test";
    private static final UserCredentials CREDENTIALS = new UserCredentials(USERNAME, PASSWORD);
    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GymMapper gymMapper;

    @Mock
    private AuthenticationComponent authComponent;

    @InjectMocks
    private TraineeService traineeService;

    @Test
    void create_CreateAndReturnTraineeResponse_RequestIsValid() {
        CreateTraineeRequest request = new CreateTraineeRequest(
                new FullName("John", "Doe"), LocalDate.of(2007, 3, 25), "Home 21 Street"
        );
        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(TRAINEE_ID);
        TraineeSummary expectedResponse = new TraineeSummary(
                TRAINEE_ID, new UserProfile(USERNAME),
                LocalDate.of(2007, 3, 25), "Home 21 Street"
        );
        when(userRepository.findUsernamesByBaseName(anyString())).thenReturn(Collections.emptyList());
        when(gymMapper.toTraineeEntity(eq(request), anySet())).thenReturn(trainee);
        when(traineeRepository.create(trainee)).thenReturn(trainee);
        when(gymMapper.toTraineeSummary(trainee)).thenReturn(expectedResponse);

        TraineeSummary actualResponse = traineeService.create(request);

        assertEquals(expectedResponse, actualResponse);
        verify(userRepository, times(1)).findUsernamesByBaseName(anyString());
        verify(gymMapper, times(1)).toTraineeEntity(eq(request), anySet());
        verify(traineeRepository, times(1)).create(trainee);
        verify(gymMapper, times(1)).toTraineeSummary(trainee);
    }

    @Test
    void update_UpdateAndReturnResponse_TraineeExists() {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                CREDENTIALS, TRAINEE_ID, new FullName("John", "Doe"),
                LocalDate.of(2007, 3, 25), "Home 21 Street"
        );
        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(TRAINEE_ID);
        TraineeSummary expectedResponse = new TraineeSummary(
                TRAINEE_ID, new UserProfile(USERNAME),
                LocalDate.of(2007, 3, 25), "Home 21 Street"
        );

        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.of(trainee));
        when(gymMapper.toTraineeEntity(request, trainee))
                .thenReturn(trainee);
        when(traineeRepository.update(trainee)).thenReturn(trainee);
        when(gymMapper.toTraineeSummary(trainee)).thenReturn(expectedResponse);

        TraineeSummary actualResponse = traineeService.update(request);

        assertEquals(expectedResponse, actualResponse);
        verify(authComponent, times(1)).authenticate(CREDENTIALS);
        verify(traineeRepository, times(1)).getById(TRAINEE_ID);
        verify(gymMapper, times(1)).toTraineeEntity(request, trainee);
        verify(traineeRepository, times(1)).update(trainee);
        verify(gymMapper, times(1)).toTraineeSummary(trainee);
    }

    @Test
    void update_ThrowEntityNotFoundException_TraineeDoesNotExist() {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                CREDENTIALS, TRAINEE_ID, new FullName("John", "Doe"),
                LocalDate.of(2007, 3, 25), "Home 21 Street");

        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> traineeService.update(request));
        assertTrue(exception.getMessage().contains("not found"));
        verify(authComponent, times(1)).authenticate(CREDENTIALS);
        verify(traineeRepository, times(1)).getById(TRAINEE_ID);
        verify(traineeRepository, never()).update(any());
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

        TraineeSummary actualResponse = traineeService.getByUsername(CREDENTIALS);
        assertEquals(expectedResponse, actualResponse);
        verify(authComponent, times(1)).authenticate(CREDENTIALS);
        verify(traineeRepository, times(1)).findByUsername(USERNAME);
        verify(gymMapper, times(1)).toTraineeSummary(trainee);
    }

    @Test
    void getByUsername_ThrowEntityNotFoundException_TraineeDoesNotExist() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> traineeService.getByUsername(CREDENTIALS));
        assertTrue(exception.getMessage().contains("not found"));
        verify(authComponent, times(1)).authenticate(CREDENTIALS);
        verify(traineeRepository, times(1)).findByUsername(USERNAME);
    }

    @Test
    void getByUsername_ThrowEntityNotFoundException_TraineeInactive() {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(new UserEntity());
        trainee.getUser().setIsActive(false);

        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> traineeService.getByUsername(CREDENTIALS));
        assertTrue(exception.getMessage().contains("inactive"));
        verify(authComponent, times(1)).authenticate(CREDENTIALS);
        verify(traineeRepository, times(1)).findByUsername(USERNAME);
    }

    @Test
    void deleteByUsername_Delete_TraineeExists() {
        traineeService.deleteByUsername(CREDENTIALS);
        verify(authComponent, times(1)).authenticate(CREDENTIALS);
        verify(traineeRepository, times(1)).deleteByUsername(USERNAME);
    }

    @Test
    void changePassword_Change_RequestIsValid(){
        String newPassword = "New_Password";
        ChangePasswordRequest request = new ChangePasswordRequest(CREDENTIALS, newPassword);
        TraineeEntity existingTrainee = new TraineeEntity();
        existingTrainee.setUser(new UserEntity());
        existingTrainee.getUser().setPassword(PASSWORD);
        existingTrainee.getUser().setIsActive(true);

        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(existingTrainee));

        traineeService.changePassword(request);

        verify(authComponent, times(1)).authenticate(CREDENTIALS);
        verify(traineeRepository, times(1)).findByUsername(USERNAME);
        verify(traineeRepository, times(1)).update(existingTrainee);
    }

    @Test
    void changePassword_ThrowEntityNotFoundException_TraineeDoesNotExist(){
        String newPassword = "New_Password";
        ChangePasswordRequest request = new ChangePasswordRequest(CREDENTIALS, newPassword);

        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> traineeService.changePassword(request));
        assertTrue(exception.getMessage().contains("username not found"));
        verify(authComponent, times(1)).authenticate(CREDENTIALS);
        verify(traineeRepository, times(1)).findByUsername(USERNAME);
    }

    @Test
    void changePassword_ThrowEntityNotFoundException_TraineeIsInactive(){
        String newPassword = "New_Password";
        ChangePasswordRequest request = new ChangePasswordRequest(CREDENTIALS, newPassword);
        TraineeEntity inactiveTrainee = new TraineeEntity();
        inactiveTrainee.setUser(new UserEntity());
        inactiveTrainee.getUser().setIsActive(false);

        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(inactiveTrainee));
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> traineeService.changePassword(request));
        assertTrue(exception.getMessage().contains("inactive"));
        verify(authComponent, times(1)).authenticate(CREDENTIALS);
        verify(traineeRepository, times(1)).findByUsername(USERNAME);
    }

    @Test
    void changePassword_ThrowInvalidPasswordException_PasswordLessThan10Chars(){
        String newPassword = "short";
        ChangePasswordRequest request = new ChangePasswordRequest(CREDENTIALS, newPassword);
        TraineeEntity existingTrainee = new TraineeEntity();
        existingTrainee.setUser(new UserEntity());
        existingTrainee.getUser().setPassword(PASSWORD);
        existingTrainee.getUser().setIsActive(true);

        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(existingTrainee));
        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class,
                () -> traineeService.changePassword(request));
        assertTrue(exception.getMessage().contains("Password should be"));
        verify(authComponent, times(1)).authenticate(CREDENTIALS);
        verify(traineeRepository, times(1)).findByUsername(USERNAME);
    }

    @Test
    void changeActivity_ChangeToInactive_RequestIsValid(){
        ChangeActivityRequest request = new ChangeActivityRequest(CREDENTIALS, false);
        TraineeEntity existingTrainee = new TraineeEntity();
        existingTrainee.setUser(new UserEntity());
        existingTrainee.getUser().setIsActive(true);

        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(existingTrainee));

        traineeService.changeActivity(request);
        verify(authComponent, times(1)).authenticate(CREDENTIALS);
        verify(traineeRepository, times(1)).update(existingTrainee);
    }

    @Test
    void changeActivity_ThrowEntityNotFoundException_TraineeDoesNotExist(){
        ChangeActivityRequest request = new ChangeActivityRequest(CREDENTIALS, false);

        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> traineeService.changeActivity(request));
        assertTrue(exception.getMessage().contains("username not found"));
        verify(authComponent, times(1)).authenticate(CREDENTIALS);
        verify(traineeRepository, times(1)).findByUsername(USERNAME);
    }

    @Test
    void changeActivity_ThrowInvalidStatusTransitionException_StatusAlreadySet(){
        ChangeActivityRequest request = new ChangeActivityRequest(CREDENTIALS, true);
        TraineeEntity existingTrainee = new TraineeEntity();
        existingTrainee.setUser(new UserEntity());
        existingTrainee.getUser().setIsActive(true);

        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(existingTrainee));

        InvalidStatusTransitionException exception = assertThrows(InvalidStatusTransitionException.class,
                () -> traineeService.changeActivity(request));
        assertTrue(exception.getMessage().contains("already assigned"));
        verify(authComponent, times(1)).authenticate(CREDENTIALS);
        verify(traineeRepository, times(1)).findByUsername(USERNAME);
    }
}
