package org.example.crm.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.crm.core.dto.ChangePasswordRequest;
import org.example.crm.security.service.OwnershipVerifier;
import org.example.crm.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@Tag(name = "Users", description = "Operations related to gym users")
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
@RequestMapping(UserController.BASE_PATH)
@RequiredArgsConstructor
public class UserController {

    public static final String BASE_PATH = "/api/v1/users";

    private final UserService userService;
    private final OwnershipVerifier ownershipVerifier;

    @Operation(summary = "Change a user's password", description = "Changes a specified user's password")
    @ApiResponse(responseCode = "200", description = "Changed password of the user")
    @PreAuthorize("hasAnyRole('TRAINEE', 'TRAINER', 'ADMIN')")
    @PutMapping("/{id}/profile/password-change")
    @ResponseStatus(HttpStatus.OK)
    public void changePassword(
            @Parameter(in = ParameterIn.PATH, description = "User ID", example = "12")
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails){
        log.info("PUT /api/v1/users/change-password endpoint called");
        ownershipVerifier.verifyOwnership(id, userDetails, OwnershipVerifier.ResourceType.USER);
        userService.changePassword(id, request);
    }
}
