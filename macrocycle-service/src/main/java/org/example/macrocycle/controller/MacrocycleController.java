package org.example.macrocycle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.macrocycle.controller.request.GenerateMacrocycleJobRequest;
import org.example.macrocycle.controller.response.MacrocycleJobAccepted;
import org.example.macrocycle.controller.response.MacrocycleJobStatusResponse;
import org.example.macrocycle.repository.MacrocycleJobDocument;
import org.example.macrocycle.service.MacrocycleJobService;
import org.example.macrocycle.service.MacrocycleOrchestrator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(MacrocycleController.BASE_PATH)
@RequiredArgsConstructor
public class MacrocycleController {

    public static final String BASE_PATH = "/api/v1/macrocycles";

    private final MacrocycleJobService macrocycleJobService;
    private final MacrocycleOrchestrator macrocycleOrchestrator;

    @Operation(summary = "Start a macrocycle generation job", description = "Persists a new job and starts asynchronous generation. Called exclusively by crm-service.")
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public MacrocycleJobAccepted generate(@Valid @RequestBody GenerateMacrocycleJobRequest request) {
        log.info("POST /api/v1/macrocycles endpoint called for trainee {}", request.traineeUsername());
        MacrocycleJobDocument job = macrocycleJobService.create(request);
        macrocycleOrchestrator.runAsync(job.getId());
        log.info("POST /api/v1/macrocycles endpoint executed, job {} started", job.getId());
        return new MacrocycleJobAccepted(job.getId(), job.getStatus());
    }

    @Operation(summary = "Get macrocycle job status", description = "Returns the current status and, once ready, the generated plan")
    @GetMapping("/{jobId}")
    public MacrocycleJobStatusResponse getStatus(
            @Parameter(in = ParameterIn.PATH, description = "Macrocycle job ID")
            @PathVariable String jobId) {
        log.info("GET /api/v1/macrocycles/{jobId} endpoint called");
        MacrocycleJobDocument job = macrocycleJobService.getJob(jobId);
        return new MacrocycleJobStatusResponse(job.getId(), job.getTraineeUsername(), job.getSupervisingTrainerUsername(),
                job.getStatus(), job.getPlan(), job.getWarnings(), job.getFailureReason());
    }

    @Operation(summary = "Discard a macrocycle job", description = "Deletes a job and its generated plan")
    @DeleteMapping("/{jobId}")
    @ResponseStatus(HttpStatus.OK)
    public void discard(
            @Parameter(in = ParameterIn.PATH, description = "Macrocycle job ID")
            @PathVariable String jobId) {
        log.info("DELETE /api/v1/macrocycles/{jobId} endpoint called");
        macrocycleJobService.delete(jobId);
    }
}
