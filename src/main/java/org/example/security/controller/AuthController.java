package org.example.security.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.security.controller.dto.LoginRequest;
import org.example.security.controller.dto.LoginDetails;
import org.example.security.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@Tag(name = "Auth", description = "Operations related to authentication")
@RestController
@RequestMapping(AuthController.BASE_PATH)
@RequiredArgsConstructor
public class AuthController {

    public static final String BASE_PATH = "/api/v1/auth";

    private final AuthService authService;

    @Operation(summary = "Login", description = "Authenticates a user and returns a JWT bearer token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logged into a user's account"),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "429", description = "Too Many Requests",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginDetails login(@Valid @RequestBody LoginRequest request){
        log.info("POST /api/v1/auth/login endpoint called");
        return authService.login(request);
    }

    @Operation(summary = "Logout", description = "Blacklists the current bearer token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logged out of a user's account"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "423", description = "Locked",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(@Parameter(hidden = true) @RequestHeader("Authorization") String header){
        log.info("POST /api/v1/auth/logout endpoint called");
        authService.logout(header);
    }
}
