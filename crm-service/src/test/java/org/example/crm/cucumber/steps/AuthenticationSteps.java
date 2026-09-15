package org.example.crm.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.crm.cucumber.config.TestContext;
import org.example.crm.exception.TooManyLoginAttemptsException;
import org.example.crm.security.controller.dto.LoginDetails;
import org.example.crm.security.controller.dto.LoginRequest;
import org.example.crm.security.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class AuthenticationSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private TestContext testContext;

    private LoginRequest loginRequest;
    private final String BEARER_TOKEN = "Bearer some-valid-token";

    @Given("a valid login request with correct credentials")
    public void a_valid_login_request_with_correct_credentials() {
        loginRequest = new LoginRequest("John.Doe", "newPassword1234!");

        LoginDetails mockDetails = new LoginDetails("mock-jwt-token");
        doReturn(mockDetails).when(authService).login(any(LoginRequest.class));
    }

    @Given("a login request with invalid {string}")
    public void a_login_request_with_invalid(String fieldState) {
        switch (fieldState) {
            case "blank username" -> loginRequest = new LoginRequest("  ", "newPassword1234!");
            case "blank password" -> loginRequest = new LoginRequest("John.Doe", "  ");
            default -> throw new IllegalArgumentException("Unknown field state mapping for: " + fieldState);
        }
    }

    @Given("the system rejects the credentials")
    public void the_system_rejects_the_credentials() {
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authService).login(any(LoginRequest.class));
    }

    @Given("the system flags the account as temporarily locked")
    public void the_system_flags_the_account_as_temporarily_locked() {
        doThrow(new TooManyLoginAttemptsException("Account is temporarily locked due to too many failed login attempts"))
                .when(authService).login(any(LoginRequest.class));
    }

    @Given("the system flags the account as inactive")
    public void the_system_flags_the_account_as_inactive() {
        doThrow(new DisabledException("Account disabled"))
                .when(authService).login(any(LoginRequest.class));
    }

    @Given("an authenticated user with a valid bearer token")
    public void an_authenticated_user_with_a_valid_bearer_token() {
        testContext.setSecurityProcessor(user("John.Doe").roles("TRAINEE"));
        testContext.setBearerToken(BEARER_TOKEN);
        doNothing().when(authService).logout(BEARER_TOKEN);
    }

    @When("the user sends a POST request to log in")
    public void the_user_sends_a_post_request_to_log_in() throws Exception {
        ResultActions resultActions = mockMvc.perform(post("/api/v1/auth/login")
                .with(testContext.getSecurityProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(loginRequest)));
        testContext.setResultActions(resultActions);
    }

    @When("the user sends a POST request to log out")
    public void the_user_sends_a_post_request_to_log_out() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post("/api/v1/auth/logout")
                .with(testContext.getSecurityProcessor());
        if(testContext.getBearerToken() != null) {
            requestBuilder.header("Authorization", testContext.getBearerToken());
        }
        testContext.setResultActions(mockMvc.perform(requestBuilder));
    }

    @Then("the login details containing a token are returned")
    public void the_login_details_containing_a_token_are_returned() throws Exception {
        testContext.getResultActions()
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Then("the login is processed by the auth service")
    public void the_login_is_processed() {
        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Then("the logout is processed by the auth service")
    public void the_logout_is_processed() {
        verify(authService, times(1)).logout(testContext.getBearerToken());
    }
}
