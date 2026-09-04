package org.example.crm.training;

import org.example.crm.core.service.GymMapper;
import org.example.crm.exception.EntityNotFoundException;
import org.example.crm.trainer.messaging.TrainerWorkloadProducerService;
import org.example.crm.trainer.messaging.TrainerWorkloadUpdateEvent;
import org.example.crm.trainer.repository.TrainerEntity;
import org.example.crm.trainer.repository.TrainerRepository;
import org.example.crm.training.event.TrainingChangedEvent;
import org.example.crm.training.event.TrainingChangedListener;
import org.example.crm.training.repository.TrainingRepository;
import org.example.crm.user.controller.dto.FullName;
import org.example.crm.user.repository.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingChangedListenerTest {

    private static final String TRAINER_USERNAME = "John.Doe";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2026, 5, 12);

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private GymMapper gymMapper;

    @Mock
    private TrainerWorkloadProducerService trainerWorkloadProducerService;

    @InjectMocks
    private TrainingChangedListener trainingChangedListener;

    @Test
    void handleTrainingChanged_RecomputeAndPublishAbsoluteTotal_TrainerExists() {
        TrainingChangedEvent event = new TrainingChangedEvent(TRAINER_USERNAME, TRAINING_DATE);
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUser(new UserEntity());
        trainer.getUser().setUsername(TRAINER_USERNAME);
        TrainerWorkloadUpdateEvent workloadUpdateEvent = new TrainerWorkloadUpdateEvent(
                TRAINER_USERNAME, new FullName("John", "Doe"), true, TRAINING_DATE, 160
        );

        when(trainerRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));
        when(trainingRepository.sumDurationByTrainerAndMonthAndYear(TRAINER_USERNAME, 2026, 5)).thenReturn(160);
        when(gymMapper.toTrainerWorkloadUpdateEvent(trainer, TRAINING_DATE, 160)).thenReturn(workloadUpdateEvent);

        trainingChangedListener.handleTrainingChanged(event);

        verify(trainerRepository, times(1)).findByUsername(TRAINER_USERNAME);
        verify(trainingRepository, times(1)).sumDurationByTrainerAndMonthAndYear(TRAINER_USERNAME, 2026, 5);
        verify(gymMapper, times(1)).toTrainerWorkloadUpdateEvent(trainer, TRAINING_DATE, 160);
        verify(trainerWorkloadProducerService, times(1)).publishTrainerWorkloadUpdateEvent(workloadUpdateEvent);
    }

    @Test
    void handleTrainingChanged_ThrowEntityNotFoundException_TrainerDoesNotExist() {
        TrainingChangedEvent event = new TrainingChangedEvent(TRAINER_USERNAME, TRAINING_DATE);

        when(trainerRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> trainingChangedListener.handleTrainingChanged(event));
        assertTrue(exception.getMessage().contains("Trainer"));
        verify(trainingRepository, never()).sumDurationByTrainerAndMonthAndYear(anyString(), anyInt(), anyInt());
        verify(gymMapper, never()).toTrainerWorkloadUpdateEvent(any(), any(LocalDate.class), anyInt());
        verify(trainerWorkloadProducerService, never()).publishTrainerWorkloadUpdateEvent(any());
    }
}
