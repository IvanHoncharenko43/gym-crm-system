package org.example.trainee;

import org.example.shared.GymMapper;
import org.example.shared.PasswordGenerator;
import org.example.shared.UsernameGenerator;
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

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private GymMapper gymMapper;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @InjectMocks
    private TraineeService traineeService;

    @Test
    void create_CreateAndReturnTraineeResponse_RequestIsValid() {
        CreateTraineeRequest request = mock(CreateTraineeRequest.class);
        when(request.firstName()).thenReturn("John");
        when(request.lastName()).thenReturn("Doe");
        Trainee mappedTrainee = new Trainee();
        mappedTrainee.setFirstName("John");
        mappedTrainee.setLastName("Doe");
        Trainee savedTrainee = new Trainee();
        savedTrainee.setId(1L);
        savedTrainee.setUsername("John.Doe");
        savedTrainee.setPassword("122333test");
        TraineeResponse expectedResponse = mock(TraineeResponse.class);

        when(gymMapper.toTrainee(request)).thenReturn(mappedTrainee);
        when(usernameGenerator.generate("John", "Doe")).thenReturn("John.Doe");
        when(passwordGenerator.generate()).thenReturn("122333test");
        when(traineeRepository.create(mappedTrainee)).thenReturn(savedTrainee);
        when(gymMapper.toTraineeResponse(savedTrainee)).thenReturn(expectedResponse);

        TraineeResponse actualResponse = traineeService.create(request);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        assertEquals("John.Doe", mappedTrainee.getUsername());
        assertEquals("122333test", mappedTrainee.getPassword());
        verify(traineeRepository, times(1)).create(mappedTrainee);
    }

    @Test
    void create_ThrowIllegalArgumentException_FirstNameIsBlank() {
        CreateTraineeRequest request = new CreateTraineeRequest("", "Doe", LocalDate.now(), "123 Street");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> traineeService.create(request));
        assertEquals("First and last names are required for registration", exception.getMessage());
        verify(traineeRepository, never()).create(any());
    }

    @Test
    void create_ThrowIllegalArgumentException_FirstNameIsNull() {
        CreateTraineeRequest request = new CreateTraineeRequest(null, "Doe", LocalDate.now(), "123 Street");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> traineeService.create(request));
        assertEquals("First and last names are required for registration", exception.getMessage());
        verify(traineeRepository, never()).create(any());
    }

    @Test
    void create_ThrowIllegalArgumentException_LastNameIsBlank() {
        CreateTraineeRequest request = new CreateTraineeRequest("John", "", LocalDate.now(), "123 Street");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> traineeService.create(request));
        assertEquals("First and last names are required for registration", exception.getMessage());
        verify(traineeRepository, never()).create(any());
    }

    @Test
    void create_ThrowIllegalArgumentException_LastNameIsNull() {
        CreateTraineeRequest request = new CreateTraineeRequest("John", null, LocalDate.now(), "123 Street");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> traineeService.create(request));
        assertEquals("First and last names are required for registration", exception.getMessage());
        verify(traineeRepository, never()).create(any());
    }

    @Test
    void update_UpdateAndReturnResponse_TraineeExists() {
        UpdateTraineeRequest request = mock(UpdateTraineeRequest.class);
        when(request.id()).thenReturn(1L);
        Trainee existingTrainee = new Trainee();
        existingTrainee.setId(1L);
        existingTrainee.setUsername("John.Doe");
        existingTrainee.setPassword("122333test");
        Trainee mappedTrainee = new Trainee();
        Trainee updatedTrainee = new Trainee();
        updatedTrainee.setId(1L);
        TraineeResponse expectedResponse = mock(TraineeResponse.class);

        when(traineeRepository.getById(1L)).thenReturn(Optional.of(existingTrainee));
        when(gymMapper.toTrainee(request)).thenReturn(mappedTrainee);
        when(traineeRepository.update(mappedTrainee)).thenReturn(updatedTrainee);
        when(gymMapper.toTraineeResponse(updatedTrainee)).thenReturn(expectedResponse);

        TraineeResponse actualResponse = traineeService.update(request);

        assertEquals(expectedResponse, actualResponse);
        assertEquals("John.Doe", mappedTrainee.getUsername());
        assertEquals("122333test", mappedTrainee.getPassword());
        verify(traineeRepository, times(1)).update(mappedTrainee);
    }

    @Test
    void update_ThrowIllegalArgumentException_IdIsNull() {
        UpdateTraineeRequest requestWithNullId = new UpdateTraineeRequest(null, "Jane", "Doe", LocalDate.now(), "123 Street", true);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> traineeService.update(requestWithNullId)
        );
        assertEquals("Trainee ID is required for update", exception.getMessage());
    }

    @Test
    void update_ThrowIllegalArgumentException_TraineeDoesNotExist() {
        UpdateTraineeRequest request = mock(UpdateTraineeRequest.class);
        when(request.id()).thenReturn(99L);
        when(traineeRepository.getById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> traineeService.update(request));
        assertTrue(exception.getMessage().contains("not found"));
        verify(traineeRepository, never()).update(any());
    }

    @Test
    void getById_ReturnResponse_TraineeExists() {
        Long id = 1L;
        Trainee trainee = new Trainee();
        TraineeResponse expectedResponse = mock(TraineeResponse.class);
        when(traineeRepository.getById(id)).thenReturn(Optional.of(trainee));
        when(gymMapper.toTraineeResponse(trainee)).thenReturn(expectedResponse);
        TraineeResponse actualResponse = traineeService.getById(id);

        assertEquals(expectedResponse, actualResponse);
        verify(traineeRepository, times(1)).getById(id);
    }

    @Test
    void getById_ThrowIllegalArgumentException_TraineeDoesNotExist() {
        Long id = 99L;
        when(traineeRepository.getById(id)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> traineeService.getById(id));
    }

    @Test
    void deleteById_Delete_TraineeExists() {
        Long id = 1L;
        Trainee trainee = new Trainee();
        when(traineeRepository.getById(id)).thenReturn(Optional.of(trainee));
        traineeService.deleteById(id);
        verify(traineeRepository, times(1)).deleteById(id);
    }

    @Test
    void deleteById_ThrowIllegalArgumentException_TraineeDoesNotExist() {
        Long id = 99L;
        when(traineeRepository.getById(id)).thenReturn(Optional.empty());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> traineeService.deleteById(id));
        assertTrue(exception.getMessage().contains("not found"));
        verify(traineeRepository, never()).deleteById(any());
    }

    @Test
    void deleteById_ThrowIllegalArgumentException_IdIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> traineeService.deleteById(null)
        );
        assertEquals("ID cannot be null", exception.getMessage());
    }
}
