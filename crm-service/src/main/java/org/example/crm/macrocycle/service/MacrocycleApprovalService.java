package org.example.crm.macrocycle.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.crm.exception.AccessForbiddenException;
import org.example.crm.exception.EntityNotFoundException;
import org.example.crm.exception.InvalidRequestDataException;
import org.example.crm.macrocycle.client.response.MacrocycleJobStatusClientResponse;
import org.example.crm.macrocycle.controller.response.MacrocycleApprovalResponse;
import org.example.crm.macrocycle.controller.response.SkippedSession;
import org.example.crm.macrocycle.plan.JobStatus;
import org.example.crm.macrocycle.plan.PlannedSession;
import org.example.crm.macrocycle.plan.PlannedWeek;
import org.example.crm.trainee.repository.TraineeEntity;
import org.example.crm.trainee.repository.TraineeRepository;
import org.example.crm.training.controller.request.CreateTrainingRequest;
import org.example.crm.training.controller.response.TrainingSummary;
import org.example.crm.training.service.TrainingService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MacrocycleApprovalService {

    private final MacrocycleClientService macrocycleClientService;
    private final TrainingService trainingService;
    private final TraineeRepository traineeRepository;
    private final Validator validator;

    public MacrocycleApprovalResponse approve(Long traineeId, String jobId) {
        TraineeEntity trainee = traineeRepository.findById(traineeId)
                .orElseThrow(() -> new EntityNotFoundException("Trainee with ID " + traineeId + " not found"));
        String traineeUsername = trainee.getUser().getUsername();

        MacrocycleJobStatusClientResponse status = macrocycleClientService.getStatus(jobId);
        if (status.status() != JobStatus.READY || status.plan() == null) {
            String message = "Macrocycle job " + jobId + " is not ready for approval";
            log.warn(message);
            throw new InvalidRequestDataException(message);
        }
        if (!traineeUsername.equals(status.traineeUsername())) {
            throw new AccessForbiddenException("Macrocycle job " + jobId + " does not belong to this trainee");
        }
        String supervisingTrainerUsername = status.supervisingTrainerUsername();

        List<Long> createdTrainingIds = new ArrayList<>();
        List<SkippedSession> skippedSessions = new ArrayList<>();

        for (PlannedWeek week : status.plan().weeks()) {
            for (PlannedSession session : week.sessions()) {
                approveSession(session, traineeUsername, supervisingTrainerUsername, createdTrainingIds, skippedSessions);
            }
        }

        log.info("Approved macrocycle job {}: created {} trainings, skipped {}",
                jobId, createdTrainingIds.size(), skippedSessions.size());
        return new MacrocycleApprovalResponse(createdTrainingIds, skippedSessions);
    }

    private void approveSession(PlannedSession session, String traineeUsername, String supervisingTrainerUsername,
                                 List<Long> createdTrainingIds, List<SkippedSession> skippedSessions) {
        LocalDate trainingDate = LocalDate.now()
                .with(TemporalAdjusters.nextOrSame(session.dayOfWeek()))
                .plusWeeks(session.weekNumber() - 1L);
        String trainingName = "[%s] %s".formatted(session.trainingType(), session.title());

        CreateTrainingRequest createRequest = new CreateTrainingRequest(
                supervisingTrainerUsername, traineeUsername, trainingName, trainingDate, session.durationMinutes());

        Set<ConstraintViolation<CreateTrainingRequest>> violations = validator.validate(createRequest);
        if (!violations.isEmpty()) {
            String reason = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("; "));
            skippedSessions.add(new SkippedSession(session.weekNumber(), session.dayOfWeek(), reason));
            return;
        }

        try {
            TrainingSummary summary = trainingService.create(createRequest);
            createdTrainingIds.add(summary.id());
        } catch (RuntimeException e) {
            log.warn("Skipping session week {} {} during macrocycle approval: {}",
                    session.weekNumber(), session.dayOfWeek(), e.getMessage());
            skippedSessions.add(new SkippedSession(session.weekNumber(), session.dayOfWeek(), e.getMessage()));
        }
    }
}
