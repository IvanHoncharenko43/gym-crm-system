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
import jakarta.ws.rs.Path;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
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
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Tag(name = "Trainers", description = "Operations related to gym trainers")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content(schema = @Schema(
                implementation = ProblemDetail.class))),
})
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/v1/trainers")
@Component
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
    @POST
    public Response registerTrainer(@Valid CreateTrainerRequest request){
        log.info("POST /api/v1/trainers endpoint called");
        return Response.status(Response.Status.CREATED)
                .entity(trainerService.create(request))
                .build();
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
    @GET
    @Path("/{id}")
    public TrainerSummary getTrainer(
            @Parameter(in = ParameterIn.PATH, description = "Trainer ID", example = "12")
            @PathParam("id") Long id,
            @Context HttpServletRequest httpServletRequest){
        log.info("GET /api/v1/trainers/{id} endpoint called");
        UserCredentials credentials = (UserCredentials) httpServletRequest.getAttribute("userCredentials");
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
    @PUT
    @Path("/{id}")
    public TrainerSummary updateTrainer(
            @Parameter(in = ParameterIn.PATH, description = "Trainer ID", example = "12")
            @PathParam("id") Long id,
            @Valid UpdateTrainerRequest request,
            @Context HttpServletRequest httpServletRequest){
        log.info("PUT /api/v1/trainers/{id} endpoint called");
        UserCredentials credentials = (UserCredentials) httpServletRequest.getAttribute("userCredentials");
        return trainerService.update(id, request, credentials);
    }

    @Operation(summary = "Get not assigned trainers by trainee", description = "Returns a list of trainers that are not assigned to a specific trainee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(
                    implementation = Trainers.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @GET
    @Path("/not-assigned")
    public Trainers getNotAssignedOnTraineeActiveTrainersList(
            @NotBlank(message = "Trainee username cannot be blank")
            @QueryParam("trainee-username") String traineeUsername,
            @Context HttpServletRequest httpServletRequest){
        log.info("GET /api/v1/trainers/not-assigned endpoint called");
        UserCredentials credentials = (UserCredentials) httpServletRequest.getAttribute("userCredentials");
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
    @PATCH
    @Path("/{id}/profile/active-status/change")
    public Response changeTrainerActivity(
            @Parameter(in = ParameterIn.PATH, description = "Trainer ID", example = "12")
            @PathParam("id") Long id,
            @Context HttpServletRequest httpServletRequest){
        log.info("PATCH /api/v1/trainers/{id}/profile/active-status/change endpoint called");
        UserCredentials credentials = (UserCredentials) httpServletRequest.getAttribute("userCredentials");
        trainerService.changeActivity(id, credentials);
        return Response.status(Response.Status.OK).build();
    }

    @Operation(summary = "Get trainer's trainings", description = "Returns an existing trainer's trainings list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(
                    implementation = Trainings.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @POST
    @Path("/trainings/search")
    public Trainings getTrainerTrainingList(
            @Valid GetTrainerTrainingsRequest request,
            @Context HttpServletRequest httpServletRequest
    ){
        log.info("GET /api/v1/trainers/trainings/search endpoint called");
        UserCredentials credentials = (UserCredentials) httpServletRequest.getAttribute("userCredentials");
        return trainingService.getTrainerTrainingList(request, credentials);
    }
}
