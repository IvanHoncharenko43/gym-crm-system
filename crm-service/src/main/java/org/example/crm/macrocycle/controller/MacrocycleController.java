package org.example.crm.macrocycle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.crm.macrocycle.controller.request.GenerateMacrocycleRequest;
import org.example.crm.macrocycle.controller.response.MacrocycleApprovalResponse;
import org.example.crm.macrocycle.controller.response.MacrocycleJobAccepted;
import org.example.crm.macrocycle.controller.response.MacrocycleJobStatusResponse;
import org.example.crm.macrocycle.service.MacrocycleApprovalService;
import org.example.crm.macrocycle.service.MacrocycleService;
import org.example.crm.security.service.OwnershipVerifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@Tag(name = "Macrocycles", description = "AI-generated long-form training macrocycles for trainees")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content(schema = @Schema(
                implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(
                implementation = ProblemDetail.class))),
})
@RestController
@RequestMapping(MacrocycleController.BASE_PATH)
@RequiredArgsConstructor
public class MacrocycleController {

    public static final String BASE_PATH = "/api/v1/trainees/{id}/macrocycles";

    private final MacrocycleService macrocycleService;
    private final MacrocycleApprovalService macrocycleApprovalService;
    private final OwnershipVerifier ownershipVerifier;

    @Operation(summary = "Request a macrocycle", description = "Starts asynchronous generation of an AI-driven multi-week training plan")
    @ApiResponse(responseCode = "202", description = "Macrocycle generation accepted")
    @PreAuthorize("hasAnyRole('TRAINEE', 'ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public MacrocycleJobAccepted generate(
            @Parameter(in = ParameterIn.PATH, description = "Trainee ID", example = "12")
            @PathVariable Long id,
            @Valid @RequestBody GenerateMacrocycleRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("POST /api/v1/trainees/{id}/macrocycles endpoint called with request");
        ownershipVerifier.verifyOwnership(id, userDetails, OwnershipVerifier.ResourceType.TRAINEE);
        return macrocycleService.requestGeneration(id, request);
    }

    @Operation(summary = "Get macrocycle status", description = "Returns the current status and, once ready, the generated plan")
    @ApiResponse(responseCode = "200", description = "Retrieved macrocycle job status")
    @PreAuthorize("hasAnyRole('TRAINEE', 'ADMIN')")
    @GetMapping("/{jobId}")
    @ResponseStatus(HttpStatus.OK)
    public MacrocycleJobStatusResponse getStatus(
            @Parameter(in = ParameterIn.PATH, description = "Trainee ID", example = "12")
            @PathVariable Long id,
            @Parameter(in = ParameterIn.PATH, description = "Macrocycle job ID")
            @PathVariable String jobId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /api/v1/trainees/{id}/macrocycles/{jobId} endpoint called");
        ownershipVerifier.verifyOwnership(id, userDetails, OwnershipVerifier.ResourceType.TRAINEE);
        return macrocycleService.getStatus(jobId);
    }

    @Operation(summary = "Approve a macrocycle", description = "Materializes a ready plan's sessions into real trainings")
    @ApiResponse(responseCode = "200", description = "Approved the macrocycle")
    @PreAuthorize("hasAnyRole('TRAINEE', 'ADMIN')")
    @PostMapping("/{jobId}/approve")
    @ResponseStatus(HttpStatus.OK)
    public MacrocycleApprovalResponse approve(
            @Parameter(in = ParameterIn.PATH, description = "Trainee ID", example = "12")
            @PathVariable Long id,
            @Parameter(in = ParameterIn.PATH, description = "Macrocycle job ID")
            @PathVariable String jobId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("POST /api/v1/trainees/{id}/macrocycles/{jobId}/approve endpoint called");
        ownershipVerifier.verifyOwnership(id, userDetails, OwnershipVerifier.ResourceType.TRAINEE);
        return macrocycleApprovalService.approve(id, jobId);
    }

    @Operation(summary = "Discard a macrocycle", description = "Deletes a macrocycle job and its generated plan")
    @ApiResponse(responseCode = "200", description = "Discarded the macrocycle")
    @PreAuthorize("hasAnyRole('TRAINEE', 'ADMIN')")
    @DeleteMapping("/{jobId}")
    @ResponseStatus(HttpStatus.OK)
    public void discard(
            @Parameter(in = ParameterIn.PATH, description = "Trainee ID", example = "12")
            @PathVariable Long id,
            @Parameter(in = ParameterIn.PATH, description = "Macrocycle job ID")
            @PathVariable String jobId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("DELETE /api/v1/trainees/{id}/macrocycles/{jobId} endpoint called");
        ownershipVerifier.verifyOwnership(id, userDetails, OwnershipVerifier.ResourceType.TRAINEE);
        macrocycleService.discardJob(jobId);
    }
}
