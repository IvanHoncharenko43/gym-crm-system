package org.example.crm.training.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.crm.security.service.OwnershipVerifier;
import org.example.crm.training.controller.request.CreateTrainingRequest;
import org.example.crm.training.controller.response.TrainingSummary;
import org.example.crm.trainingType.dto.TrainingTypes;
import org.example.crm.training.service.TrainingService;
import org.example.crm.trainingType.service.TrainingTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Trainings", description = "Operations related to gym trainings")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content(schema = @Schema(
                implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(
                implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "423", description = "Locked", content = @Content(schema = @Schema(
                implementation = ProblemDetail.class)))
})
@RestController
@RequestMapping(TrainingController.BASE_PATH)
@RequiredArgsConstructor
public class TrainingController {

    public static final String BASE_PATH = "/api/v1/trainings";

    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;
    private final OwnershipVerifier ownershipVerifier;

    @Operation(summary = "Add a new training", description = "Creates a new training and returns its summary")
    @ApiResponse(responseCode = "200", description = "Added a new training")
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public TrainingSummary addTraining(@Valid @RequestBody CreateTrainingRequest request,
                                       @AuthenticationPrincipal UserDetails userDetails){
        log.info("POST /api/v1/trainings endpoint called");
        ownershipVerifier.verifyOwnership(request.trainerUsername(), userDetails);
        return trainingService.create(request);
    }

    @Operation(summary = "Cancel a training", description = "Cancels an existing training by ID")
    @ApiResponse(responseCode = "200", description = "Cancelled the training")
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void cancelTraining(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("DELETE /api/v1/trainings/{} endpoint called", id);
        ownershipVerifier.verifyOwnership(id, userDetails, OwnershipVerifier.ResourceType.TRAINING);
        trainingService.cancel(id);
    }

    @Operation(summary = "Get training types", description = "Returns a list of all training types")
    @ApiResponse(responseCode = "200", description = "Retrieved all training types")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/training-types")
    @ResponseStatus(HttpStatus.OK)
    public TrainingTypes getTrainingTypes(){
        log.info("GET /api/v1/trainings/training-types endpoint called");
        return trainingTypeService.getAllTrainingTypes();
    }
}