package org.example.macrocycle.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.macrocycle.config.MacrocycleGenerationProperties;
import org.example.macrocycle.controller.request.GenerateMacrocycleJobRequest;
import org.example.macrocycle.exception.MacrocycleJobNotFoundException;
import org.example.macrocycle.plan.JobStatus;
import org.example.macrocycle.plan.MacrocyclePlan;
import org.example.macrocycle.repository.MacrocycleJobDocument;
import org.example.macrocycle.repository.MacrocycleJobRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(MacrocycleGenerationProperties.class)
public class MacrocycleJobService {

    private final MacrocycleJobRepository macrocycleJobRepository;
    private final MacrocycleGenerationProperties properties;

    public MacrocycleJobDocument create(GenerateMacrocycleJobRequest request) {
        MacrocycleJobDocument job = new MacrocycleJobDocument();
        job.setTraineeUsername(request.traineeUsername());
        job.setSupervisingTrainerUsername(request.supervisingTrainerUsername());
        job.setGoal(request.goal());
        job.setTargetEventDate(request.targetEventDate());
        job.setInjuriesAndConstraints(request.injuriesAndConstraints());
        job.setWeeklyAvailability(request.weeklyAvailability());
        job.setPreferredSessionDurationMinutes(request.preferredSessionDurationMinutes());
        job.setContextSnapshot(request.contextSnapshot());
        job.setStatus(JobStatus.PENDING);
        job.setIterationCount(0);

        MacrocycleJobDocument saved = macrocycleJobRepository.save(job);
        log.info("Created macrocycle job {} for trainee {}", saved.getId(), saved.getTraineeUsername());
        return saved;
    }

    private static final Set<JobStatus> TERMINAL_STATUSES = EnumSet.of(JobStatus.READY, JobStatus.FAILED);

    public MacrocycleJobDocument getJob(String jobId) {
        return macrocycleJobRepository.findById(jobId)
                .orElseThrow(() -> new MacrocycleJobNotFoundException("Macrocycle job " + jobId + " not found"));
    }

    public void markGenerating(String jobId, int iterationCount) {
        MacrocycleJobDocument job = getJob(jobId);
        if (isTerminal(job)) {
            return;
        }
        job.setStatus(JobStatus.GENERATING);
        job.setIterationCount(iterationCount);
        macrocycleJobRepository.save(job);
    }

    public void markVerifying(String jobId) {
        MacrocycleJobDocument job = getJob(jobId);
        if (isTerminal(job)) {
            return;
        }
        job.setStatus(JobStatus.VERIFYING);
        macrocycleJobRepository.save(job);
    }

    public void markReady(String jobId, MacrocyclePlan plan, Set<String> warnings) {
        MacrocycleJobDocument job = getJob(jobId);
        if (isTerminal(job)) {
            log.warn("Ignoring late READY result for macrocycle job {} already in terminal state {}", jobId, job.getStatus());
            return;
        }
        job.setStatus(JobStatus.READY);
        job.setPlan(plan);
        job.setWarnings(warnings);
        markCompleted(job);
    }

    public void markFailed(String jobId, String reason) {
        MacrocycleJobDocument job = getJob(jobId);
        if (isTerminal(job)) {
            return;
        }
        job.setStatus(JobStatus.FAILED);
        job.setFailureReason(reason);
        markCompleted(job);
    }

    private boolean isTerminal(MacrocycleJobDocument job) {
        return TERMINAL_STATUSES.contains(job.getStatus());
    }

    private void markCompleted(MacrocycleJobDocument job) {
        job.setExpiresAt(LocalDateTime.now().plusDays(properties.jobRetentionDays()));
        macrocycleJobRepository.save(job);
        log.info("Macrocycle job {} completed with status {}", job.getId(), job.getStatus());
    }

    public void delete(String jobId) {
        macrocycleJobRepository.deleteById(jobId);
        log.info("Deleted macrocycle job {}", jobId);
    }
}
