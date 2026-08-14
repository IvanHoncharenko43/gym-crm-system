package org.example.training.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.training.controller.request.CreateTrainingRequest;
import org.example.training.controller.response.TrainingSummary;
import org.example.trainingType.dto.TrainingTypes;
import org.example.training.service.TrainingService;
import org.example.trainingType.service.TrainingTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@Tag(name = "Trainings", description = "Operations related to gym trainings")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content(schema = @Schema(
                implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                implementation = ProblemDetail.class)))
})
@RestController
@RequestMapping("/api/v1/trainings")
public class TrainingController {

    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;

    public TrainingController(TrainingService trainingService, TrainingTypeService trainingTypeService){
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
    }

    @Operation(summary = "Add a new training", description = "Creates a new training and returns its summary")
    @ApiResponse(responseCode = "200", description = "Added a new training")
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public TrainingSummary addTraining(@Valid @RequestBody CreateTrainingRequest request){
        log.info("POST /api/v1/trainings endpoint called");
        return trainingService.create(request);
    }

    @Operation(summary = "Get training types", description = "Returns a list of all training types")
    @ApiResponse(responseCode = "200", description = "Retrieved all training types")
    @GetMapping("/training-types")
    @ResponseStatus(HttpStatus.OK)
    public TrainingTypes getTrainingTypes(){
        log.info("GET /api/v1/trainings/training-types endpoint called");
        return trainingTypeService.getAllTrainingTypes();
    }
}