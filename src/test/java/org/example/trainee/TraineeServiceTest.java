package org.example.trainee;

import org.example.exception.NotFoundException;
import org.example.shared.FullName;
import org.example.shared.GymMapper;
import org.example.shared.UserProfile;
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
public class TraineeServiceTest {

    private static final Long TRAINEE_ID = 1L;
    private static final String USERNAME = "John.Doe";
    private static final String PASSWORD = "122333test";

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private GymMapper gymMapper;

    @InjectMocks
    private TraineeService traineeService;

    @Test
    void create_CreateAndReturnTraineeResponse_RequestIsValid() {
        CreateTraineeRequest request = new CreateTraineeRequest(
                new FullName("John", "Doe"), LocalDate.of(2007, 3, 25), "Home 21 Street"
        );
        TraineeEntity mappedTrainee = new TraineeEntity();
        TraineeEntity savedTrainee = new TraineeEntity();
        savedTrainee.setId(TRAINEE_ID);
        TraineeSummary expectedResponse = new TraineeSummary(
                TRAINEE_ID, new UserProfile(USERNAME),
                LocalDate.of(2007, 3, 25), "Home 21 Street"
        );

        when(gymMapper.toTraineeEntity(request)).thenReturn(mappedTrainee);
        when(traineeRepository.create(mappedTrainee)).thenReturn(savedTrainee);
        when(gymMapper.toTraineeSummary(savedTrainee)).thenReturn(expectedResponse);

        TraineeSummary actualResponse = traineeService.create(request);

        assertEquals(expectedResponse, actualResponse);
        verify(gymMapper, times(1)).toTraineeEntity(request);
        verify(traineeRepository, times(1)).create(mappedTrainee);
        verify(gymMapper, times(1)).toTraineeSummary(savedTrainee);
    }

    @Test
    void update_UpdateAndReturnResponse_TraineeExists() {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                TRAINEE_ID, new FullName("John", "Doe"),
                LocalDate.of(2007, 3, 25), "Home 21 Street", true
        );
        TraineeEntity existingTrainee = new TraineeEntity();
        existingTrainee.setId(1L);
        existingTrainee.setUsername(USERNAME);
        existingTrainee.setPassword(PASSWORD);
        TraineeEntity mappedTrainee = new TraineeEntity();
        TraineeEntity updatedTrainee = new TraineeEntity();
        updatedTrainee.setId(TRAINEE_ID);
        TraineeSummary expectedResponse = new TraineeSummary(
                TRAINEE_ID, new UserProfile(USERNAME),
                LocalDate.of(2007, 3, 25), "Home 21 Street"
        );

        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.of(existingTrainee));
        when(gymMapper.toTraineeEntity(request, USERNAME, PASSWORD))
                .thenReturn(mappedTrainee);
        when(traineeRepository.update(mappedTrainee)).thenReturn(updatedTrainee);
        when(gymMapper.toTraineeSummary(updatedTrainee)).thenReturn(expectedResponse);

        TraineeSummary actualResponse = traineeService.update(request);

        assertEquals(expectedResponse, actualResponse);
        verify(traineeRepository, times(1)).getById(TRAINEE_ID);
        verify(gymMapper, times(1)).toTraineeEntity(request, USERNAME, PASSWORD);
        verify(traineeRepository, times(1)).update(mappedTrainee);
        verify(gymMapper, times(1)).toTraineeSummary(updatedTrainee);
    }

    @Test
    void update_ThrowNotFoundException_TraineeDoesNotExist() {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                TRAINEE_ID, new FullName("John", "Doe"),
                LocalDate.of(2007, 3, 25), "Home 21 Street", true);

        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> traineeService.update(request));
        assertTrue(exception.getMessage().contains("Trainee not found"));
        verify(traineeRepository, times(1)).getById(TRAINEE_ID);
        verify(traineeRepository, never()).update(any());
    }

    @Test
    void getById_ReturnResponse_TraineeExists() {
        TraineeEntity trainee = new TraineeEntity();
        TraineeSummary expectedResponse = new TraineeSummary(
                TRAINEE_ID, new UserProfile(USERNAME),
                LocalDate.of(2007, 3, 25), "Home 21 Street"
        );

        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.of(trainee));
        when(gymMapper.toTraineeSummary(trainee)).thenReturn(expectedResponse);

        TraineeSummary actualResponse = traineeService.getById(TRAINEE_ID);
        assertEquals(expectedResponse, actualResponse);
        verify(traineeRepository, times(1)).getById(TRAINEE_ID);
        verify(gymMapper, times(1)).toTraineeSummary(trainee);
    }

    @Test
    void getById_ThrowNotFoundException_TraineeDoesNotExist() {
        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> traineeService.getById(TRAINEE_ID));
        verify(traineeRepository, times(1)).getById(TRAINEE_ID);
    }

    @Test
    void getById_ThrowNotFoundException_TraineeInactive() {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setActive(false);

        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.of(trainee));

        assertThrows(NotFoundException.class, () -> traineeService.getById(TRAINEE_ID));
        verify(traineeRepository, times(1)).getById(TRAINEE_ID);
    }

    @Test
    void deleteById_Delete_TraineeExists() {
        TraineeEntity trainee = new TraineeEntity();
        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.of(trainee));
        traineeService.deleteById(TRAINEE_ID);
        verify(traineeRepository, times(1)).deleteById(TRAINEE_ID);
    }
}
