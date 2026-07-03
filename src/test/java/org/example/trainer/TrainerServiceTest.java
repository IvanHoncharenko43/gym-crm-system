package org.example.trainer;

import org.example.exception.NotFoundException;
import org.example.shared.*;
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

    private static final Long TRAINER_ID = 1L;
    private static final String USERNAME = "John.Doe";
    private static final String PASSWORD = "122333test";

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private GymMapper gymMapper;

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void create_CreateAndReturnTrainerResponse_RequestIsValid() {
        CreateTrainerRequest request = new CreateTrainerRequest(
                new FullName("John", "Doe"), TrainingType.YOGA
        );
        TrainerEntity mappedTrainer = new TrainerEntity();
        TrainerEntity savedTrainer = new TrainerEntity();
        savedTrainer.setId(TRAINER_ID);
        TrainerSummary expectedResponse = new TrainerSummary(
                TRAINER_ID, new UserProfile(USERNAME),
                TrainingType.YOGA
        );

        when(gymMapper.toTrainerEntity(request)).thenReturn(mappedTrainer);
        when(trainerRepository.create(mappedTrainer)).thenReturn(savedTrainer);
        when(gymMapper.toTrainerSummary(savedTrainer)).thenReturn(expectedResponse);

        TrainerSummary actualResponse = trainerService.create(request);

        assertEquals(expectedResponse, actualResponse);
        verify(gymMapper, times(1)).toTrainerEntity(request);
        verify(trainerRepository, times(1)).create(mappedTrainer);
        verify(gymMapper, times(1)).toTrainerSummary(savedTrainer);
    }

    @Test
    void update_UpdateAndReturnResponse_TrainerExists() {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                TRAINER_ID, new FullName("John", "Doe"),
                TrainingType.YOGA, true
        );
        TrainerEntity existingTrainer = new TrainerEntity();
        existingTrainer.setId(1L);
        existingTrainer.setUsername("Jane.Smith");
        existingTrainer.setPassword("test122333");
        TrainerEntity mappedTrainer = new TrainerEntity();
        TrainerEntity updatedTrainer = new TrainerEntity();
        updatedTrainer.setId(TRAINER_ID);
        TrainerSummary expectedResponse = new TrainerSummary(
                TRAINER_ID, new UserProfile(USERNAME),
                TrainingType.YOGA
        );

        when(trainerRepository.getById(TRAINER_ID)).thenReturn(Optional.of(existingTrainer));
        when(gymMapper.toTrainerEntity(request, USERNAME, PASSWORD))
                .thenReturn(mappedTrainer);
        when(trainerRepository.update(mappedTrainer)).thenReturn(updatedTrainer);
        when(gymMapper.toTrainerSummary(updatedTrainer)).thenReturn(expectedResponse);

        TrainerSummary actualResponse = trainerService.update(request);

        assertEquals(expectedResponse, actualResponse);
        verify(trainerRepository, times(1)).getById(TRAINER_ID);
        verify(gymMapper, times(1)).toTrainerEntity(request, USERNAME, PASSWORD);
        verify(trainerRepository, times(1)).update(mappedTrainer);
        verify(gymMapper, times(1)).toTrainerSummary(updatedTrainer);
    }

    @Test
    void update_ThrowNotFoundException_TrainerDoesNotExist() {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                TRAINER_ID, new FullName("John", "Doe"),
                TrainingType.YOGA, true
        );

        when(trainerRepository.getById(TRAINER_ID)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> trainerService.update(request));
        assertTrue(exception.getMessage().contains("Trainer not found"));
        verify(trainerRepository, times(1)).getById(TRAINER_ID);
        verify(trainerRepository, never()).update(any());
    }

    @Test
    void getById_ReturnResponse_TrainerExists() {
        TrainerEntity trainer = new TrainerEntity();
        TrainerSummary expectedResponse = new TrainerSummary(
                TRAINER_ID, new UserProfile(USERNAME),
                TrainingType.YOGA
        );

        when(trainerRepository.getById(TRAINER_ID)).thenReturn(Optional.of(trainer));
        when(gymMapper.toTrainerSummary(trainer)).thenReturn(expectedResponse);

        TrainerSummary actualResponse = trainerService.getById(TRAINER_ID);
        assertEquals(expectedResponse, actualResponse);
        verify(trainerRepository, times(1)).getById(TRAINER_ID);
        verify(gymMapper, times(1)).toTrainerSummary(trainer);
    }

    @Test
    void getById_ThrowNotFoundException_TrainerDoesNotExist() {
        when(trainerRepository.getById(TRAINER_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> trainerService.getById(TRAINER_ID));
        verify(trainerRepository, times(1)).getById(TRAINER_ID);
    }

    @Test
    void getById_ThrowNotFoundException_TrainerInactive() {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setActive(false);

        when(trainerRepository.getById(TRAINER_ID)).thenReturn(Optional.of(trainer));

        assertThrows(NotFoundException.class, () -> trainerService.getById(TRAINER_ID));
        verify(trainerRepository, times(1)).getById(TRAINER_ID);
    }
}
