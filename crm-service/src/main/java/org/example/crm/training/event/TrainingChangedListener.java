package org.example.crm.training.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.crm.core.service.GymMapper;
import org.example.crm.exception.EntityNotFoundException;
import org.example.crm.trainer.messaging.TrainerWorkloadProducerService;
import org.example.crm.trainer.messaging.TrainerWorkloadUpdateEvent;
import org.example.crm.trainer.repository.TrainerEntity;
import org.example.crm.trainer.repository.TrainerRepository;
import org.example.crm.training.repository.TrainingRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrainingChangedListener {

    private final TrainingRepository trainingRepository;
    private final TrainerRepository trainerRepository;
    private final GymMapper gymMapper;
    private final TrainerWorkloadProducerService trainerWorkloadProducerService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTrainingChanged(TrainingChangedEvent event){
        log.info("Recalculating absolute workload for trainer");
        int year = event.trainingDate().getYear();
        int monthValue = event.trainingDate().getMonthValue();
        TrainerEntity trainer = trainerRepository.findByUsername(event.trainerUsername())
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found during workload update"));
        int totalDurationMinutes = trainingRepository.sumDurationByTrainerAndMonthAndYear(
                event.trainerUsername(), year, monthValue
        );
        TrainerWorkloadUpdateEvent trainerWorkloadUpdateEvent = gymMapper.toTrainerWorkloadUpdateEvent(trainer, event.trainingDate(), totalDurationMinutes);
        trainerWorkloadProducerService.publishTrainerWorkloadUpdateEvent(trainerWorkloadUpdateEvent);
    }
}
