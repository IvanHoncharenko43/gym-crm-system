package org.example.security;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.example.config.SecurityConfig;
import org.example.security.controller.AuthController;
import org.example.security.controller.dto.LoginDetails;
import org.example.security.controller.dto.LoginRequest;
import org.example.security.service.AuthService;
import org.example.security.service.JwtService;
import org.example.security.service.TokenBlackListService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.example.TestUtils.*;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.Mockito.*;

@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private TokenBlackListService tokenBlackListService;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Test
    void login_Return200AndLoginDetails_CredentialsAreValid() {
        LoginRequest request = getLoginRequest();
        LoginDetails expectedResult = new LoginDetails("token");
        when(authService.login(request)).thenReturn(expectedResult);

        LoginDetails result = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .status(HttpStatus.OK)
                .extract()
                .as(LoginDetails.class);

        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo(expectedResult.token());
        verify(authService, times(1)).login(request);
    }

    @Test
    void login_Return400AndProblemDetail_UsernameIsBlank() {
        LoginRequest request = new LoginRequest("  ", TRAINEE_PASSWORD);

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("title", equalTo("Bad Request"))
                .body("invalidFields", hasKey("username"));
    }

    @Test
    void login_Return400AndProblemDetail_PasswordIsBlank() {
        LoginRequest request = new LoginRequest(TRAINEE_USERNAME, "  ");

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("title", equalTo("Bad Request"))
                .body("invalidFields", hasKey("password"));
    }

    @Test
    void login_Return401AndProblemDetail_CredentialsAreInvalid() {
        LoginRequest request = getLoginRequest();
        when(authService.login(request)).thenThrow(new BadCredentialsException("Bad credentials"));

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .status(HttpStatus.UNAUTHORIZED)
                .body("title", equalTo("Authentication Failure"))
                .body("detail", equalTo("Invalid username or password"));
    }

    @Test
    void login_Return401AndProblemDetail_AccountIsLocked() {
        LoginRequest request = getLoginRequest();
        when(authService.login(request)).thenThrow(new LockedException("Account locked"));

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .status(HttpStatus.UNAUTHORIZED)
                .body("title", equalTo("Authentication Failure"))
                .body("detail", equalTo("Account is temporarily locked due to too many failed login attempts"));
    }

    @Test
    void login_Return401AndProblemDetail_AccountIsInactive() {
        LoginRequest request = getLoginRequest();
        when(authService.login(request)).thenThrow(new DisabledException("Account disabled"));

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .status(HttpStatus.UNAUTHORIZED)
                .body("title", equalTo("Authentication Failure"))
                .body("detail", equalTo("The account is inactive"));
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD, roles = {"TRAINEE"})
    void logout_Return200_TokenIsValid() {
        String authorizationHeader = "Bearer some-token";
        doNothing().when(authService).logout(authorizationHeader);

        given()
                .header("Authorization", authorizationHeader)
                .when()
                .post("/api/v1/auth/logout")
                .then()
                .status(HttpStatus.OK);
        verify(authService, times(1)).logout(authorizationHeader);
    }

    @Test
    @WithAnonymousUser
    void logout_Return401AndProblemDetail_UserIsUnauthenticated() {
        given()
                .header("Authorization", "Bearer some-token")
                .when()
                .post("/api/v1/auth/logout")
                .then()
                .status(HttpStatus.UNAUTHORIZED)
                .body("title", equalTo("Authentication Failure"))
                .body("detail", equalTo("Authentication failed during accessing the resource"));
        verify(authService, never()).logout(any());
    }
}
