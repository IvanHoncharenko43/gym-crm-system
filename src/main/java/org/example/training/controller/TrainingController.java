package org.example.training.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Context;
import lombok.extern.slf4j.Slf4j;
import org.example.training.controller.request.CreateTrainingRequest;
import org.example.training.controller.response.TrainingSummary;
import org.example.trainingType.dto.TrainingTypes;
import org.example.training.service.TrainingService;
import org.example.trainingType.service.TrainingTypeService;
import org.example.user.controller.dto.UserCredentials;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

@Slf4j
@Tag(name = "Trainings", description = "Operations related to gym trainings")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content(schema = @Schema(
                implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                implementation = ProblemDetail.class)))
})
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/v1/trainings")
@Component
public class TrainingController {

    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;

    public TrainingController(TrainingService trainingService, TrainingTypeService trainingTypeService){
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
    }

    @Operation(summary = "Add a new training", description = "Creates a new training and returns its summary")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(
            implementation = TrainingSummary.class)))
    @POST
    public TrainingSummary addTraining(@Valid CreateTrainingRequest request,
                                       @Context HttpServletRequest httpServletRequest){
        log.info("POST /api/v1/trainings endpoint called");
        UserCredentials credentials = (UserCredentials) httpServletRequest.getAttribute("userCredentials");
        return trainingService.create(request, credentials);
    }

    @Operation(summary = "Get training types", description = "Returns a list of all training types")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(
            implementation = TrainingTypes.class)))
    @GET
    @Path("/training-types")
    public TrainingTypes getTrainingTypes(
            @Context HttpServletRequest httpServletRequest
    ){
        log.info("GET /api/v1/trainings/training-types endpoint called");
        UserCredentials credentials = (UserCredentials) httpServletRequest.getAttribute("userCredentials");
        return trainingTypeService.getAllTrainingTypes(credentials);
    }
}
