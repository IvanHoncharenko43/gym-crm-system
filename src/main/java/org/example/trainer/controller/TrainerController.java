package org.example.trainer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.example.trainer.controller.request.CreateTrainerRequest;
import org.example.trainer.controller.request.GetTrainerTrainingsRequest;
import org.example.trainer.controller.response.TrainerSummary;
import org.example.trainer.controller.request.UpdateTrainerRequest;
import org.example.trainer.controller.response.Trainers;
import org.example.trainer.service.TrainerService;
import org.example.training.controller.response.Trainings;
import org.example.training.service.TrainingService;
import org.example.user.controller.dto.UserCredentials;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@Tag(name = "Trainers", description = "Operations related to gym trainers")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content(schema = @Schema(
                implementation = ProblemDetail.class))),
})
@RestController
@RequestMapping("/api/v1/trainers")
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public TrainerController(TrainerService trainerService, TrainingService trainingService){
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    @Operation(summary = "Register a new trainer", description = "Creates a new trainer profile and returns their summary")
    @ApiResponse(responseCode = "201", description = "CREATED", content = @Content(schema = @Schema(
            implementation = TrainerSummary.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrainerSummary registerTrainer(@Valid @RequestBody CreateTrainerRequest request){
        log.info("POST /api/v1/trainers endpoint called");
        return trainerService.create(request);
    }

    @Operation(summary = "Get trainer", description = "Returns a single trainer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(
                    implementation = TrainerSummary.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Trainer Not Found", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TrainerSummary getTrainer(
            @Parameter(in = ParameterIn.PATH, description = "Trainer ID", example = "12")
            @PathVariable("id") Long id,
            @Parameter(hidden = true)
            @RequestAttribute("userCredentials") UserCredentials credentials){
        log.info("GET /api/v1/trainers/{id} endpoint called");
        return trainerService.getById(id, credentials);
    }

    @Operation(summary = "Update trainer", description = "Updates an existing trainer's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(
                    implementation = TrainerSummary.class
            ))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Trainer Not Found", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TrainerSummary updateTrainer(
            @Parameter(in = ParameterIn.PATH, description = "Trainer ID", example = "12")
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateTrainerRequest request,
            @Parameter(hidden = true)
            @RequestAttribute("userCredentials") UserCredentials credentials){
        log.info("PUT /api/v1/trainers/{id} endpoint called");
        return trainerService.update(id, request, credentials);
    }

    @Operation(summary = "Get not assigned trainers by trainee", description = "Returns a list of trainers that are not assigned to a specific trainee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(
                    implementation = Trainers.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @GetMapping(value = "/not-assigned", params = "trainee-username")
    @ResponseStatus(HttpStatus.OK)
    public Trainers getNotAssignedOnTraineeActiveTrainersList(
            @NotBlank(message = "Trainee username cannot be blank")
            @RequestParam("trainee-username") String traineeUsername,
            @Parameter(hidden = true)
            @RequestAttribute("userCredentials") UserCredentials credentials){
        log.info("GET /api/v1/trainers/not-assigned endpoint called");
        return trainerService.getUnassignedTrainersByTraineeList(traineeUsername, credentials);
    }

    @Operation(summary = "Change trainer activity", description = "Changes a trainer's activity status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Trainer Not Found", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @PatchMapping(value = "/{id}/activity-change")
    @ResponseStatus(HttpStatus.OK)
    public void changeTrainerActivity(
            @Parameter(in = ParameterIn.PATH, description = "Trainer ID", example = "12")
            @PathVariable("id") Long id,
            @Parameter(hidden = true)
            @RequestAttribute("userCredentials") UserCredentials credentials){
        log.info("PATCH /api/v1/trainers/{id}/activity-change endpoint called");
        trainerService.changeActivity(id, credentials);
    }

    @Operation(summary = "Get trainer's trainings", description = "Returns an existing trainer's trainings list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(
                    implementation = Trainings.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @GetMapping(value = "/trainings")
    @ResponseStatus(HttpStatus.OK)
    public Trainings getTrainerTrainingList(
            @ParameterObject @Valid GetTrainerTrainingsRequest request,
            @Parameter(hidden = true)
            @RequestAttribute("userCredentials") UserCredentials credentials
    ){
        log.info("GET /api/v1/trainers/trainings/search endpoint called");
        return trainingService.getTrainerTrainingList(request, credentials);
    }
}
