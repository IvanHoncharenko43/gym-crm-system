package org.example.crm.trainer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.crm.security.service.OwnershipVerifier;
import org.example.crm.trainer.controller.request.TrainerMonthlyWorkloadRequest;
import org.example.crm.trainer.controller.response.TrainerWorkloadSummary;
import org.example.crm.trainer.controller.request.CreateTrainerRequest;
import org.example.crm.trainer.controller.request.GetTrainerTrainingsRequest;
import org.example.crm.trainer.controller.response.TrainerSummary;
import org.example.crm.trainer.controller.request.UpdateTrainerRequest;
import org.example.crm.trainer.controller.response.Trainers;
import org.example.crm.trainer.service.TrainerService;
import org.example.crm.trainer.service.TrainerWorkloadService;
import org.example.crm.training.controller.response.Trainings;
import org.example.crm.training.service.TrainingService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@Validated
@Tag(name = "Trainers", description = "Operations related to gym trainers")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content(schema = @Schema(
                implementation = ProblemDetail.class))),
})
@RestController
@RequestMapping(TrainerController.BASE_PATH)
@RequiredArgsConstructor
public class TrainerController {

    public static final String BASE_PATH = "/api/v1/trainers";

    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final OwnershipVerifier ownershipVerifier;
    private final TrainerWorkloadService trainerWorkloadService;

    @Operation(summary = "Register a new trainer", description = "Creates a new trainer profile and returns their summary")
    @ApiResponse(responseCode = "201", description = "Registered a new trainer")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrainerSummary registerTrainer(@Valid @RequestBody CreateTrainerRequest request){
        log.info("POST /api/v1/trainers endpoint called");
        return trainerService.create(request);
    }

    @Operation(summary = "Get trainer", description = "Returns a single trainer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved the trainer"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Trainer Not Found", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "423", description = "Locked", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TrainerSummary getTrainer(
            @Parameter(in = ParameterIn.PATH, description = "Trainer ID", example = "12")
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails){
        log.info("GET /api/v1/trainers/{id} endpoint called");
        ownershipVerifier.verifyOwnership(id, userDetails, OwnershipVerifier.ResourceType.TRAINER);
        return trainerService.getById(id);
    }

    @Operation(summary = "Update trainer", description = "Updates an existing trainer's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated the trainer"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Trainer Not Found", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "423", description = "Locked", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TrainerSummary updateTrainer(
            @Parameter(in = ParameterIn.PATH, description = "Trainer ID", example = "12")
            @PathVariable Long id,
            @Valid @RequestBody UpdateTrainerRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails){
        log.info("PUT /api/v1/trainers/{id} endpoint called");
        ownershipVerifier.verifyOwnership(id, userDetails, OwnershipVerifier.ResourceType.TRAINER);
        return trainerService.update(id, request);
    }

    @Operation(summary = "Get not assigned trainers by trainee", description = "Returns a list of trainers that are not assigned to a specific trainee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved not assigned on the trainee trainers"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "423", description = "Locked", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('TRAINEE', 'ADMIN')")
    @GetMapping(value = "/not-assigned", params = "trainee-username")
    @ResponseStatus(HttpStatus.OK)
    public Trainers getNotAssignedOnTraineeActiveTrainersList(
            @NotBlank(message = "Trainee username cannot be blank")
            @RequestParam("trainee-username") String traineeUsername,
            @AuthenticationPrincipal UserDetails userDetails){
        log.info("GET /api/v1/trainers/not-assigned endpoint called");
        ownershipVerifier.verifyOwnership(traineeUsername, userDetails);
        return trainerService.getUnassignedTrainersByTraineeList(traineeUsername);
    }

    @Operation(summary = "Change trainer activity", description = "Changes a trainer's activity status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Changed activity of the trainer"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Trainer Not Found", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "423", description = "Locked", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @PatchMapping(value = "/{id}/profile/active-status/change")
    @ResponseStatus(HttpStatus.OK)
    public void changeTrainerActivity(
            @Parameter(in = ParameterIn.PATH, description = "Trainer ID", example = "12")
            @PathVariable Long id,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails){
        log.info("PATCH /api/v1/trainers/{id}/profile/active-status/change endpoint called");
        ownershipVerifier.verifyOwnership(id, userDetails, OwnershipVerifier.ResourceType.TRAINER);
        trainerService.changeActivity(id);
    }

    @Operation(summary = "Get trainer's trainings", description = "Returns an existing trainer's trainings list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved trainee's trainings"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "423", description = "Locked", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @PostMapping(value = "/trainings/search")
    @ResponseStatus(HttpStatus.OK)
    public Trainings getTrainerTrainingList(
            @Valid @RequestBody GetTrainerTrainingsRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        log.info("GET /api/v1/trainers/trainings/search endpoint called");
        ownershipVerifier.verifyOwnership(request.username(), userDetails);
        return trainingService.getTrainerTrainingList(request);
    }

    @Operation(summary = "Get a trainer's monthly workload summary",
            description = "Retrieves a trainer's total training duration for a given month and year")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer's monthly workload summary"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Trainer Not Found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "423", description = "Locked", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "Workload Service Unavailable",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @GetMapping("/workloads")
    @ResponseStatus(HttpStatus.OK)
    public TrainerWorkloadSummary getTrainerWorkload(@Valid @ParameterObject TrainerMonthlyWorkloadRequest request) {
        log.info("GET /api/v1/trainers/workload endpoint called");
        TrainerWorkloadSummary response = trainerWorkloadService.getWorkload(request);
        log.info("GET /api/v1/trainers/workload endpoint executed");
        return response;
    }
}
