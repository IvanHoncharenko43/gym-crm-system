package org.example.trainee.controller;

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
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.example.trainee.controller.request.CreateTraineeRequest;
import org.example.trainee.controller.response.TraineeSummary;
import org.example.trainee.controller.request.UpdateTraineeRequest;
import org.example.trainee.controller.request.UpdateTraineeTrainersRequest;
import org.example.trainee.controller.request.GetTraineeTrainingsRequest;
import org.example.trainee.service.TraineeService;
import org.example.trainer.controller.response.Trainers;
import org.example.training.controller.response.Trainings;
import org.example.training.service.TrainingService;
import org.example.user.controller.dto.UserCredentials;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Tag(name = "Trainees", description = "Operations related to gym trainees")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content(schema = @Schema(
                implementation = ProblemDetail.class))),
})
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/v1/trainees")
@Component
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainingService trainingService;

    public TraineeController(TraineeService traineeService, TrainingService trainingService){
        this.traineeService = traineeService;
        this.trainingService = trainingService;
    }

    @Operation(summary = "Register a new trainee", description = "Creates a new trainee profile and returns their summary")
    @ApiResponse(responseCode = "201", description = "CREATED", content = @Content(schema = @Schema(
            implementation = TraineeSummary.class)))
    @POST
    public Response registerTrainee(
            @Valid CreateTraineeRequest request){
        log.info("POST /api/v1/trainees endpoint called with request");
        return Response.status(Response.Status.CREATED)
                .entity(traineeService.create(request))
                .build();
    }

    @Operation(summary = "Get trainee", description = "Returns a single trainee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(
                    implementation = TraineeSummary.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Trainee Not Found", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @GET
    @Path("/{id}")
    public TraineeSummary getTrainee(
            @Parameter(in = ParameterIn.PATH, description = "Trainee ID", example = "12")
            @PathParam("id") Long id,
            @Context HttpServletRequest httpServletRequest){
        log.info("GET /api/v1/trainees/{id} endpoint called");
        UserCredentials credentials = (UserCredentials) httpServletRequest.getAttribute("userCredentials");
        return traineeService.getById(id, credentials);
    }

    @Operation(summary = "Update trainee", description = "Updates an existing trainee's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(
                    implementation = TraineeSummary.class
            ))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Trainee Not Found", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @PUT
    @Path("/{id}")
    public TraineeSummary updateTrainee(
            @Parameter(in = ParameterIn.PATH, description = "Trainee ID", example = "12")
            @PathParam("id") Long id,
            @Valid UpdateTraineeRequest request,
            @Context HttpServletRequest httpServletRequest){
        log.info("PUT /api/v1/trainees/{id} endpoint called with request");
        UserCredentials credentials = (UserCredentials) httpServletRequest.getAttribute("userCredentials");
        return traineeService.update(id, request, credentials);
    }

    @Operation(summary = "Delete trainee", description = "Deletes a trainee's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @DELETE
    public Response deleteTrainee(
            @Parameter(in = ParameterIn.QUERY, description = "Trainee's username", example = "John.Doe1")
            @NotBlank(message = "Username cannot be null")
            @QueryParam("username") String username,
            @Context HttpServletRequest httpServletRequest){
        log.info("DELETE /api/v1/trainees endpoint called");
        UserCredentials credentials = (UserCredentials) httpServletRequest.getAttribute("userCredentials");
        traineeService.deleteByUsername(username, credentials);
        return Response.status(Response.Status.OK).build();
    }

    @Operation(summary = "Update trainee's trainers", description = "Updates a trainee's trainers list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(
                    implementation = Trainers.class
            ))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Trainee Not Found", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @PUT
    @Path("/{id}/trainers-update")
    public Trainers updateTrainersList(
            @Parameter(in = ParameterIn.PATH, description = "Trainee ID", example = "12")
            @PathParam("id") Long id,
            @Valid UpdateTraineeTrainersRequest request,
            @Context HttpServletRequest httpServletRequest){
        log.info("PUT /api/v1/trainees/{id}/trainers-update endpoint called with request");
        UserCredentials credentials = (UserCredentials) httpServletRequest.getAttribute("userCredentials");
        return traineeService.updateTrainersList(id, request, credentials);
    }

    @Operation(summary = "Change trainee activity", description = "Changes a trainee's activity status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Trainee Not Found", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @PATCH
    @Path("/{id}/profile/active-status/change")
    public Response changeTraineeActivity(
            @Parameter(in = ParameterIn.PATH, description = "Trainee ID", example = "12")
            @PathParam("id") Long id,
            @Context HttpServletRequest httpServletRequest){
        log.info("PATCH /api/v1/trainees/{id}/profile/active-status/change endpoint called");
        UserCredentials credentials = (UserCredentials) httpServletRequest.getAttribute("userCredentials");
        traineeService.changeActivity(id, credentials);
        return Response.status(Response.Status.OK).build();
    }

    @Operation(summary = "Get trainee's trainings", description = "Returns an existing trainee's trainings list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(
                    implementation = Trainings.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class)))
    })
    @GET
    @Path("/trainings")
    public Trainings getTraineeTrainingList(
            @BeanParam @Valid GetTraineeTrainingsRequest request,
            @Context HttpServletRequest httpServletRequest
    ){
        log.info("GET /api/v1/trainees/trainings endpoint called with request params");
        UserCredentials credentials = (UserCredentials) httpServletRequest.getAttribute("userCredentials");
        return trainingService.getTraineeTrainingList(request, credentials);
    }
}
