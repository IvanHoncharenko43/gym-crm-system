package org.example.user.controller;

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
import org.example.core.dto.ChangePasswordRequest;
import org.example.user.controller.dto.UserCredentials;
import org.example.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;

@Slf4j
@Tag(name = "Users", description = "Operations related to gym users")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content(schema = @Schema(
                implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(
                implementation = ProblemDetail.class)))
})
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @Operation(summary = "Change a user's password", description = "Changes a specified user's password")
    @ApiResponse(responseCode = "200", description = "Changed password of the user")
    @PutMapping("/{id}/profile/password-change")
    @ResponseStatus(HttpStatus.OK)
    public void changePassword(
            @Parameter(in = ParameterIn.PATH, description = "User ID", example = "12")
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request,
            @Parameter(hidden = true)
            @RequestAttribute UserCredentials userCredentials){
        log.info("PUT /api/v1/users/change-password endpoint called");
        userService.changePassword(id, request, userCredentials);
    }
}
