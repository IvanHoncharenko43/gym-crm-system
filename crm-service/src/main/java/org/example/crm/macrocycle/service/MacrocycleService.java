package org.example.crm.macrocycle.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.crm.exception.EntityNotFoundException;
import org.example.crm.exception.InvalidRequestDataException;
import org.example.crm.macrocycle.client.request.GenerateMacrocycleJobRequest;
import org.example.crm.macrocycle.client.response.MacrocycleJobAcceptedResponse;
import org.example.crm.macrocycle.client.response.MacrocycleJobStatusClientResponse;
import org.example.crm.macrocycle.controller.request.GenerateMacrocycleRequest;
import org.example.crm.macrocycle.controller.response.MacrocycleJobAccepted;
import org.example.crm.macrocycle.controller.response.MacrocycleJobStatusResponse;
import org.example.crm.macrocycle.plan.TraineeContextSnapshot;
import org.example.crm.macrocycle.plan.TrainingHistoryItem;
import org.example.crm.trainee.repository.TraineeEntity;
import org.example.crm.trainee.repository.TraineeRepository;
import org.example.crm.training.repository.TrainingEntity;
import org.example.crm.training.repository.TrainingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MacrocycleService {

    private static final int HISTORY_LOOKBACK_DAYS = 90;
    private static final int HISTORY_MAX_ITEMS = 30;

    private final TraineeRepository traineeRepository;
    private final TrainingRepository trainingRepository;
    private final MacrocycleClientService macrocycleClientService;

    @Transactional(readOnly = true)
    public MacrocycleJobAccepted requestGeneration(Long traineeId, GenerateMacrocycleRequest request) {
        TraineeEntity trainee = traineeRepository.findById(traineeId)
                .orElseThrow(() -> new EntityNotFoundException("Trainee with ID " + traineeId + " not found"));

        boolean trainerAssigned = trainee.getTrainers().stream()
                .anyMatch(trainer -> trainer.getUser().getUsername().equals(request.supervisingTrainerUsername()));
        if (!trainerAssigned) {
            String message = "Supervising trainer '" + request.supervisingTrainerUsername()
                    + "' is not assigned to this trainee. Assign them first via PUT /api/v1/trainees/" + traineeId + "/trainers-update";
            log.warn(message);
            throw new InvalidRequestDataException(message);
        }

        String traineeUsername = trainee.getUser().getUsername();
        TraineeContextSnapshot contextSnapshot = buildContextSnapshot(traineeUsername);

        GenerateMacrocycleJobRequest clientRequest = new GenerateMacrocycleJobRequest(
                traineeUsername,
                request.supervisingTrainerUsername(),
                request.goal(),
                request.targetEventDate(),
                request.injuriesAndConstraints(),
                request.weeklyAvailability(),
                request.preferredSessionDurationMinutes(),
                contextSnapshot
        );

        MacrocycleJobAcceptedResponse response = macrocycleClientService.generate(clientRequest);
        return new MacrocycleJobAccepted(response.jobId(), response.status());
    }

    public MacrocycleJobStatusResponse getStatus(String jobId) {
        MacrocycleJobStatusClientResponse response = macrocycleClientService.getStatus(jobId);
        return new MacrocycleJobStatusResponse(response.jobId(), response.status(), response.plan(),
                response.warnings(), response.failureReason());
    }

    public void discardJob(String jobId) {
        macrocycleClientService.discard(jobId);
    }

    private TraineeContextSnapshot buildContextSnapshot(String traineeUsername) {
        LocalDate cutoff = LocalDate.now().minusDays(HISTORY_LOOKBACK_DAYS);
        List<TrainingHistoryItem> history = trainingRepository.findAllByTraineeUserUsername(traineeUsername).stream()
                .filter(training -> !training.getTrainingDate().isBefore(cutoff))
                .sorted(Comparator.comparing(TrainingEntity::getTrainingDate).reversed())
                .limit(HISTORY_MAX_ITEMS)
                .map(training -> new TrainingHistoryItem(
                        training.getTrainingDate(),
                        training.getTrainingType().getTrainingTypeName().name(),
                        training.getDurationMinutes(),
                        training.getTrainingName()))
                .toList();
        return new TraineeContextSnapshot(traineeUsername, history);
    }
}
