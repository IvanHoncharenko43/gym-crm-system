package org.example.user.controller;

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
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.example.core.dto.ChangePasswordRequest;
import org.example.user.controller.dto.UserCredentials;
import org.example.user.service.UserService;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

@Slf4j
@Tag(name = "Users", description = "Operations related to gym users")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content(schema = @Schema(
                implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(
                implementation = ProblemDetail.class)))
})
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/v1/users")
@Component
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @Operation(summary = "Change a user's password", description = "Changes a specified user's password")
    @ApiResponse(responseCode = "200", description = "OK")
    @PUT
    @Path("/{id}/profile/password-change")
    public Response changePassword(
            @Parameter(in = ParameterIn.PATH, description = "User ID", example = "12")
            @PathParam("id") Long id,
            @Valid ChangePasswordRequest request,
            @Context HttpServletRequest httpServletRequest
            ){
        log.info("PUT /api/v1/users/profile/password-change endpoint called");
        UserCredentials credentials = (UserCredentials) httpServletRequest.getAttribute("userCredentials");
        userService.changePassword(id, request, credentials);
        return Response.status(Response.Status.OK).build();
    }
}
