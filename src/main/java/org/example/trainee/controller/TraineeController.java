package org.example.trainee.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.trainee.controller.request.CreateTraineeRequest;
import org.example.trainee.controller.request.GetTraineeTrainingsRequest;
import org.example.trainee.controller.response.TraineeSummary;
import org.example.trainee.controller.request.UpdateTraineeRequest;
import org.example.trainee.controller.request.UpdateTraineeTrainersRequest;
import org.example.trainee.service.TraineeService;
import org.example.trainer.controller.response.Trainers;
import org.example.training.controller.response.Trainings;
import org.example.training.service.TrainingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@Validated
@Tag(name = "Trainees", description = "Operations related to gym trainees")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content(schema = @Schema(
                implementation = ProblemDetail.class))),
})
@RestController
@RequestMapping("/api/v1/trainees")
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainingService trainingService;

    public TraineeController(TraineeService traineeService, TrainingService trainingService){
        this.traineeService = traineeService;
        this.trainingService = trainingService;
    }

    @Operation(summary = "Register a new trainee", description = "Creates a new trainee profile and returns their summary")
    @ApiResponse(responseCode = "201", description = "Registered the trainee")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TraineeSummary registerTrainee(
            @Valid @RequestBody CreateTraineeRequest request){
        log.info("POST /api/v1/trainees endpoint called with request");
        return traineeService.create(request);
    }

    @Operation(summary = "Get trainee", description = "Returns a single trainee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved a new trainee"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Trainee Not Found", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TraineeSummary getTrainee(
            @Parameter(in = ParameterIn.PATH, description = "Trainee ID", example = "12")
            @PathVariable Long id){
        log.info("GET /api/v1/trainees/{id} endpoint called");
        return traineeService.getById(id);
    }

    @Operation(summary = "Update trainee", description = "Updates an existing trainee's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated the trainee"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Trainee Not Found", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TraineeSummary updateTrainee(
            @Parameter(in = ParameterIn.PATH, description = "Trainee ID", example = "12")
            @PathVariable Long id,
            @Valid @RequestBody UpdateTraineeRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails){
        log.info("PUT /api/v1/trainees/{id} endpoint called with request");
        return traineeService.update(id, request, userDetails);
    }

    @Operation(summary = "Delete trainee", description = "Deletes a trainee's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deleted the trainee"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @DeleteMapping(params = "username")
    @ResponseStatus(HttpStatus.OK)
    public void deleteTrainee(
            @Parameter(in = ParameterIn.QUERY, description = "Trainee's username", example = "John.Doe")
            @RequestParam String username,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails){
        log.info("DELETE /api/v1/trainees endpoint called");
        traineeService.deleteByUsername(username, userDetails);
    }

    @Operation(summary = "Update trainee's trainers", description = "Updates a trainee's trainers list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated trainers of the trainee"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Trainee Not Found", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @PutMapping(value = "/{id}/trainers-update")
    @ResponseStatus(HttpStatus.OK)
    public Trainers updateTrainersList(
            @Parameter(in = ParameterIn.PATH, description = "Trainee ID", example = "12")
            @PathVariable Long id,
            @Valid @RequestBody UpdateTraineeTrainersRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails){
        log.info("PUT /api/v1/trainees/{id}/trainers-update endpoint called with request");
        return traineeService.updateTrainersList(id, request, userDetails);
    }

    @Operation(summary = "Change trainee activity", description = "Changes a trainee's activity status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Changed activity of the trainee"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Trainee Not Found", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @PatchMapping(value = "/{id}/profile/active-status/change")
    @ResponseStatus(HttpStatus.OK)
    public void changeTraineeActivity(
            @Parameter(in = ParameterIn.PATH, description = "Trainee ID", example = "12")
            @PathVariable Long id,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails){
        log.info("PATCH /api/v1/trainees/{id}/profile/active-status/change endpoint called");
        traineeService.changeActivity(id, userDetails);
    }

    @Operation(summary = "Get trainee's trainings", description = "Returns an existing trainee's trainings list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved trainee's trainings"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @PostMapping(value = "/trainings/search")
    @ResponseStatus(HttpStatus.OK)
    public Trainings getTraineeTrainingList(
            @Valid @RequestBody GetTraineeTrainingsRequest request
    ){
        log.info("GET /api/v1/trainees/trainings/search endpoint called with request params");
        return trainingService.getTraineeTrainingList(request);
    }
}
