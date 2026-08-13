package org.example.security.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.security.controller.dto.LoginRequest;
import org.example.security.controller.dto.LoginResponse;
import org.example.security.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Auth", description = "Operations related to authentication")
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login", description = "Authenticates a user and returns a JWT bearer token")
    @ApiResponse(responseCode = "200", description = "Logged into a user's account")
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@Valid @RequestBody LoginRequest request){
        log.info("POST /api/v1/auth/login endpoint called");
        return authService.login(request);
    }

    @Operation(summary = "Logout", description = "Blacklists the current bearer token")
    @ApiResponse(responseCode = "200", description = "Logged out of a user's account")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(@RequestHeader("Authorization") String header){
        log.info("POST /api/v1/auth/logout endpoint called");
        authService.logout(header);
    }
}
