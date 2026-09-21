package org.example.macrocycle.service;

import lombok.extern.slf4j.Slf4j;
import org.example.macrocycle.config.MacrocycleGenerationProperties;
import org.example.macrocycle.exception.PlanGenerationFailedException;
import org.example.macrocycle.plan.MacrocyclePlan;
import org.example.macrocycle.plan.VerifierIssue;
import org.example.macrocycle.plan.VerifierVerdict;
import org.example.macrocycle.repository.MacrocycleJobDocument;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@EnableConfigurationProperties(MacrocycleGenerationProperties.class)
public class MacrocycleOrchestrator {

    private final MacrocycleJobService macrocycleJobService;
    private final PlannerAgentService plannerAgentService;
    private final VerifierAgentService verifierAgentService;
    private final MacrocycleGenerationProperties properties;
    private final ThreadPoolTaskExecutor macrocycleTaskExecutor;

    public MacrocycleOrchestrator(MacrocycleJobService macrocycleJobService,
                                   PlannerAgentService plannerAgentService,
                                   VerifierAgentService verifierAgentService,
                                   MacrocycleGenerationProperties properties,
                                   @Qualifier("macrocycleTaskExecutor") ThreadPoolTaskExecutor macrocycleTaskExecutor) {
        this.macrocycleJobService = macrocycleJobService;
        this.plannerAgentService = plannerAgentService;
        this.verifierAgentService = verifierAgentService;
        this.properties = properties;
        this.macrocycleTaskExecutor = macrocycleTaskExecutor;
    }

    public void runAsync(String jobId) {
        CompletableFuture
                .runAsync(() -> executeJob(jobId), macrocycleTaskExecutor)
                .orTimeout(properties.generation().jobTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .exceptionally(throwable -> {
                    log.error("Macrocycle job {} did not complete within the configured timeout", jobId, throwable);
                    macrocycleJobService.markFailed(jobId, "Generation timed out");
                    return null;
                });
    }

    private void executeJob(String jobId) {
        try {
            MacrocycleJobDocument job = macrocycleJobService.getJob(jobId);
            int maxIterations = properties.generation().maxIterations();
            Set<String> warnings = new LinkedHashSet<>();
            List<VerifierIssue> priorIssues = null;
            MacrocyclePlan lastDraft = null;

            for (int iteration = 1; iteration <= maxIterations; iteration++) {
                macrocycleJobService.markGenerating(jobId, 1);
                MacrocyclePlan draft = generateDraft(job, priorIssues);
                lastDraft = draft;

                macrocycleJobService.markVerifying(jobId);
                VerifierVerdict verdict = verifyDraft(job, draft);

                if (verdict != null && verdict.approved()) {
                    macrocycleJobService.markReady(jobId, draft, warnings);
                    return;
                }

                priorIssues = verdict != null ? verdict.issues() : List.of();
                priorIssues.forEach(issue -> warnings.add(
                        "Week %s: %s".formatted(issue.weekNumber(), issue.description())));
            }

            if (lastDraft != null) {
                log.warn("Macrocycle job {} exhausted {} iterations without approval; returning best-effort draft",
                        jobId, maxIterations);
                macrocycleJobService.markReady(jobId, lastDraft, warnings);
            } else {
                macrocycleJobService.markFailed(jobId, "Planner failed to produce a draft plan");
            }
        } catch (PlanGenerationFailedException e) {
            log.error("Macrocycle job {} failed during generation", jobId, e);
            macrocycleJobService.markFailed(jobId, e.getMessage());
        } catch (Exception e) {
            log.error("Macrocycle job {} failed unexpectedly", jobId, e);
            macrocycleJobService.markFailed(jobId, "Unexpected error during generation");
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
}
