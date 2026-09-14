package org.example.crm.user.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.example.crm.exception.AccessForbiddenException;
import org.example.crm.security.service.OwnershipVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CommonSteps {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper jsonMapper;

    @Autowired
    protected OwnershipVerifier ownershipVerifier;

    @Autowired
    private TestContext testContext;

    @Given("an authenticated user with a role {string} and ID {long}")
    public void an_authenticated_user_with_role_and_id(String role, Long id){
        testContext.setSecurityProcessor(user("John.Doe").password("newPassword1234!").roles(role));
    }

    @Given("an authenticated user with a role {string} and username {string}")
    public void an_authenticated_user_with_a_role_and_username(String role, String username) {
        testContext.setSecurityProcessor(user(username).password("newPassword1234!").roles(role));
    }

    @Given("an anonymous user")
    public void an_anonymous_user() {
        testContext.setSecurityProcessor(anonymous());
    }

    @Given("the system rejects authorization for {string} with ID {long}")
    public void the_system_rejects_authorization_for_id(String role, Long id) {
        OwnershipVerifier.ResourceType resourceType = OwnershipVerifier.ResourceType.valueOf(role.toUpperCase());

        doThrow(new AccessForbiddenException("Authorization failed"))
                .when(ownershipVerifier).verifyOwnership(eq(id), any(), eq(resourceType));
    }

    @Given("the system rejects authorization for username {string}")
    public void the_system_rejects_authorization_for_username(String username) {
        doThrow(new AccessForbiddenException("Authorization failed"))
                .when(ownershipVerifier).verifyOwnership(eq(username), any());
    }

    @Then("the response status is {int} {}")
    public void the_response_status_is(int expectedStatus, String statusText) throws Exception {
        testContext.getResultActions().andExpect(status().is(expectedStatus));
    }

    @Then("the ownership verification is triggered for {string} with ID {long}")
    public void the_ownership_verification_is_triggered_for_id(String role, Long id) {
        OwnershipVerifier.ResourceType resourceType = OwnershipVerifier.ResourceType.valueOf(role.toUpperCase());
        verify(ownershipVerifier, times(1))
                .verifyOwnership(eq(id), any(), eq(resourceType));
    }

    @Then("the ownership verification is triggered for username {string}")
    public void the_ownership_verification_is_triggered_for_username(String username) {
        verify(ownershipVerifier, times(1)).verifyOwnership(eq(username), any());
    }

    @Then("the response is a ProblemDetail with title {string} containing invalid fields")
    public void the_response_is_a_problem_detail_with_title_containing_invalid_fields(String title) throws Exception {
        testContext.getResultActions().andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Then("the response is a ProblemDetail with title {string} and detail {string}")
    public void the_response_is_a_problem_detail_with_title_and_detail(String title, String detail) throws Exception {
        testContext.getResultActions().andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.detail").value(detail));
    }

    @Then("the response is a ProblemDetail with title {string} containing a validation error for {string}")
    public void the_response_is_a_problem_detail_with_validation_error_for(String title, String errorField) throws Exception {
        testContext.getResultActions().andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.invalidFields['" + errorField + "']").exists())
                .andExpect(jsonPath("$.invalidFields['" + errorField + "']").isNotEmpty());
    }

    @Then("the response is a ProblemDetail with title {string} containing constraint violations")
    public void the_response_is_a_problem_detail_with_title_containing_constraint_violations(String title) throws Exception {
        testContext.getResultActions().andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.violations").isNotEmpty());
    }
}
