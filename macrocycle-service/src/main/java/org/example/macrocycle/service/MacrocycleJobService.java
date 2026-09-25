package org.example.macrocycle.service;

import lombok.extern.slf4j.Slf4j;
import org.example.macrocycle.config.MacrocycleGenerationProperties;
import org.example.macrocycle.controller.request.GenerateMacrocycleJobRequest;
import org.example.macrocycle.controller.response.MacrocycleJobStatusResponse;
import org.example.macrocycle.exception.MacrocycleJobNotFoundException;
import org.example.macrocycle.exception.PlanGenerationFailedException;
import org.example.macrocycle.plan.JobStatus;
import org.example.macrocycle.plan.MacrocyclePlan;
import org.example.macrocycle.plan.VerifierIssue;
import org.example.macrocycle.plan.VerifierVerdict;
import org.example.macrocycle.repository.MacrocycleJobDocument;
import org.example.macrocycle.repository.MacrocycleJobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@EnableConfigurationProperties(MacrocycleGenerationProperties.class)
public class MacrocycleJobService {

    private final MacrocycleJobRepository macrocycleJobRepository;
    private final MacrocycleGenerationProperties properties;
    private final MacrocycleMapper macrocycleMapper;
    private final PlannerAgentService plannerAgentService;
    private final VerifierAgentService verifierAgentService;
    private final ThreadPoolTaskExecutor macrocycleTaskExecutor;
    private static final Set<JobStatus> TERMINAL_STATUSES = EnumSet.of(JobStatus.READY, JobStatus.FAILED);

    public MacrocycleJobService(MacrocycleJobRepository macrocycleJobRepository, MacrocycleGenerationProperties properties,
                                MacrocycleMapper macrocycleMapper, PlannerAgentService plannerAgentService, VerifierAgentService verifierAgentService,
                                @Qualifier("macrocycleTaskExecutor") ThreadPoolTaskExecutor macrocycleTaskExecutor){
        this.macrocycleJobRepository = macrocycleJobRepository;
        this.properties = properties;
        this.macrocycleMapper = macrocycleMapper;
        this.plannerAgentService = plannerAgentService;
        this.verifierAgentService = verifierAgentService;
        this.macrocycleTaskExecutor = macrocycleTaskExecutor;
    }

    @Transactional
    public MacrocycleJobStatusResponse create(GenerateMacrocycleJobRequest request) {
        MacrocycleJobDocument macrocycleJobDocument = macrocycleMapper.toMacrocycleJobDocument(request);
        MacrocycleJobDocument savedMacrocycleJobDocument = macrocycleJobRepository.save(macrocycleJobDocument);
        log.info("Created macrocycle job {} for trainee {}", savedMacrocycleJobDocument.getId(), savedMacrocycleJobDocument.getTraineeUsername());
        return macrocycleMapper.toMacrocycleJobStatusResponse(savedMacrocycleJobDocument);
    }

    public MacrocycleJobStatusResponse getJob(String jobId) {
        return macrocycleJobRepository.findById(jobId)
                .map(macrocycleMapper::toMacrocycleJobStatusResponse)
                .orElseThrow(() -> new MacrocycleJobNotFoundException("Macrocycle job " + jobId + " not found"));
    }

    @Transactional
    public void delete(String jobId){
        macrocycleJobRepository.deleteById(jobId);
    }

    public void runAsync(String jobId) {
        CompletableFuture
                .runAsync(() -> executeJob(jobId), macrocycleTaskExecutor)
                .orTimeout(properties.generation().jobTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .exceptionally(throwable -> {
                    log.error("Macrocycle job {} did not complete within the configured timeout", jobId, throwable);
                    markFailed(jobId, "Generation timed out");
                    return null;
                });
    }

    private void executeJob(String jobId) {
        try {
            MacrocycleJobDocument job = macrocycleJobRepository.findById(jobId)
                    .orElseThrow(() -> new MacrocycleJobNotFoundException("Macrocycle job " + jobId + " not found"));
            int maxIterations = properties.generation().maxIterations();
            Set<String> warnings = new LinkedHashSet<>();
            List<VerifierIssue> priorIssues = null;
            MacrocyclePlan lastDraft = null;

            for (int iteration = 1; iteration <= maxIterations; iteration++) {
                MacrocyclePlan draft = generateDraft(job, priorIssues);
                lastDraft = draft;

                VerifierVerdict verdict = verifyDraft(job, draft);

                if (verdict != null && verdict.approved()) {
                    markReady(jobId, draft, warnings);
                    return;
                }

                priorIssues = verdict != null ? verdict.issues() : List.of();
                priorIssues.forEach(issue -> warnings.add(
                        "Week %s: %s".formatted(issue.weekNumber(), issue.description())));
            }

            if (lastDraft != null) {
                log.warn("Macrocycle job {} exhausted {} iterations without approval; returning best-effort draft",
                        jobId, maxIterations);
                markReady(jobId, lastDraft, warnings);
            } else {
                markFailed(jobId, "Planner failed to produce a draft plan");
            }
        } catch (PlanGenerationFailedException e) {
            log.error("Macrocycle job {} failed during generation", jobId, e);
            markFailed(jobId, e.getMessage());
        } catch (Exception e) {
            log.error("Macrocycle job {} failed unexpectedly", jobId, e);
            markFailed(jobId, "Unexpected error during generation");
        }
    }

    private MacrocyclePlan generateDraft(MacrocycleJobDocument job, List<VerifierIssue> priorIssues) {
        try {
            return plannerAgentService.generate(job, priorIssues);
        } catch (Exception e) {
            throw new PlanGenerationFailedException("Planner agent failed to produce a plan", e);
        }
    }

    private VerifierVerdict verifyDraft(MacrocycleJobDocument job, MacrocyclePlan draft) {
        try {
            return verifierAgentService.verify(job, draft);
        } catch (Exception e) {
            throw new PlanGenerationFailedException("Verifier agent failed to review the plan", e);
        }
    }

    private void markReady(String jobId, MacrocyclePlan plan, Set<String> warnings) {
        MacrocycleJobDocument job = macrocycleJobRepository.findById(jobId)
                .orElseThrow(() -> new MacrocycleJobNotFoundException("Macrocycle job " + jobId + " not found"));
        if (isTerminal(job)) {
            log.warn("Ignoring late READY result for macrocycle job {} already in terminal state {}", jobId, job.getStatus());
            return;
        }
        job.setStatus(JobStatus.READY);
        job.setPlan(plan);
        job.setWarnings(warnings);
        markCompleted(job);
    }

    private void markFailed(String jobId, String reason) {
        MacrocycleJobDocument job = macrocycleJobRepository.findById(jobId)
                .orElseThrow(() -> new MacrocycleJobNotFoundException("Macrocycle job " + jobId + " not found"));
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
}
