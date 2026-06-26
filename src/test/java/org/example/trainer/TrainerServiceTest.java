package org.example.trainer;

import org.example.shared.GymMapper;
import org.example.shared.PasswordGenerator;
import org.example.shared.TrainingType;
import org.example.shared.UsernameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private GymMapper gymMapper;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void create_CreateAndReturnTrainerResponse_RequestIsValid() {
        CreateTrainerRequest request = mock(CreateTrainerRequest.class);
        when(request.firstName()).thenReturn("Jane");
        when(request.lastName()).thenReturn("Smith");
        Trainer mappedTrainer = new Trainer();
        mappedTrainer.setFirstName("Jane");
        mappedTrainer.setLastName("Smith");
        Trainer savedTrainer = new Trainer();
        savedTrainer.setId(1L);
        savedTrainer.setUsername("Jane.Smith");
        savedTrainer.setPassword("test122333");
        TrainerResponse expectedResponse = mock(TrainerResponse.class);

        when(gymMapper.toTrainer(request)).thenReturn(mappedTrainer);
        when(usernameGenerator.generate("Jane", "Smith")).thenReturn("Jane.Smith");
        when(passwordGenerator.generate()).thenReturn("test122333");
        when(trainerRepository.create(mappedTrainer)).thenReturn(savedTrainer);
        when(gymMapper.toTrainerResponse(savedTrainer)).thenReturn(expectedResponse);

        TrainerResponse actualResponse = trainerService.create(request);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        assertEquals("Jane.Smith", mappedTrainer.getUsername());
        assertEquals("test122333", mappedTrainer.getPassword());
        verify(trainerRepository, times(1)).create(mappedTrainer);
    }

    @Test
    void create_ThrowIllegalArgumentException_FirstNameIsBlank() {
        CreateTrainerRequest request = new CreateTrainerRequest("", "Doe", TrainingType.YOGA);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainerService.create(request));
        assertEquals("First and last names are required for registration", exception.getMessage());
        verify(trainerRepository, never()).create(any());
    }

    @Test
    void create_ThrowIllegalArgumentException_FirstNameIsNull() {
        CreateTrainerRequest request = new CreateTrainerRequest(null, "Doe", TrainingType.YOGA);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainerService.create(request));
        assertEquals("First and last names are required for registration", exception.getMessage());
        verify(trainerRepository, never()).create(any());
    }

    @Test
    void create_ThrowIllegalArgumentException_LastNameIsBlank() {
        CreateTrainerRequest request = new CreateTrainerRequest("John", "", TrainingType.YOGA);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainerService.create(request));
        assertEquals("First and last names are required for registration", exception.getMessage());
        verify(trainerRepository, never()).create(any());
    }

    @Test
    void create_ThrowIllegalArgumentException_LastNameIsNull() {
        CreateTrainerRequest request = new CreateTrainerRequest("John", null, TrainingType.YOGA);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainerService.create(request));
        assertEquals("First and last names are required for registration", exception.getMessage());
        verify(trainerRepository, never()).create(any());
    }

    @Test
    void update_UpdateAndReturnResponse_TrainerExists() {
        UpdateTrainerRequest request = mock(UpdateTrainerRequest.class);
        when(request.id()).thenReturn(1L);
        Trainer existingTrainer = new Trainer();
        existingTrainer.setId(1L);
        existingTrainer.setUsername("Jane.Smith");
        existingTrainer.setPassword("test122333");
        Trainer mappedTrainer = new Trainer();
        Trainer updatedTrainer = new Trainer();
        updatedTrainer.setId(1L);
        TrainerResponse expectedResponse = mock(TrainerResponse.class);

        when(trainerRepository.getById(1L)).thenReturn(Optional.of(existingTrainer));
        when(gymMapper.toTrainer(request)).thenReturn(mappedTrainer);
        when(trainerRepository.update(mappedTrainer)).thenReturn(updatedTrainer);
        when(gymMapper.toTrainerResponse(updatedTrainer)).thenReturn(expectedResponse);

        TrainerResponse actualResponse = trainerService.update(request);

        assertEquals(expectedResponse, actualResponse);
        assertEquals("Jane.Smith", mappedTrainer.getUsername());
        assertEquals("test122333", mappedTrainer.getPassword());
        verify(trainerRepository, times(1)).update(mappedTrainer);
    }

    @Test
    void update_ThrowIllegalArgumentException_IdIsNull() {
        UpdateTrainerRequest requestWithNullId = new UpdateTrainerRequest(null, "John", "Doe", TrainingType.YOGA, true);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> trainerService.update(requestWithNullId)
        );
        assertEquals("Trainer ID is required for update", exception.getMessage());
    }

    @Test
    void update_ThrowIllegalArgumentException_TrainerDoesNotExist() {
        UpdateTrainerRequest request = mock(UpdateTrainerRequest.class);
        when(request.id()).thenReturn(99L);
        when(trainerRepository.getById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainerService.update(request));
        assertTrue(exception.getMessage().contains("not found"));
        verify(trainerRepository, never()).update(any());
    }

    @Test
    void getById_ReturnResponse_TrainerExists() {
        Long id = 1L;
        Trainer trainer = new Trainer();
        TrainerResponse expectedResponse = mock(TrainerResponse.class);
        when(trainerRepository.getById(id)).thenReturn(Optional.of(trainer));
        when(gymMapper.toTrainerResponse(trainer)).thenReturn(expectedResponse);

        TrainerResponse actualResponse = trainerService.getById(id);
        assertEquals(expectedResponse, actualResponse);
        verify(trainerRepository, times(1)).getById(id);
    }

    @Test
    void getById_ThrowIllegalArgumentException_TrainerDoesNotExist() {
        Long id = 99L;
        when(trainerRepository.getById(id)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> trainerService.getById(id));
    }
}
