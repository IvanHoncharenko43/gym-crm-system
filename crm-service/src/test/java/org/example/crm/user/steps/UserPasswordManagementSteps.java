package org.example.crm.user.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.crm.core.dto.ChangePasswordRequest;
import org.example.crm.exception.EntityNotFoundException;
import org.example.crm.security.service.OwnershipVerifier;
import org.example.crm.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public class UserPasswordManagementSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private TestContext testContext;
    private ChangePasswordRequest request;

    @Given("a valid change password request")
    public void a_valid_change_password_request() {
        request = new ChangePasswordRequest("oldPassword1234", "newPassword1234!");
    }

    @Given("a change password request with old password {string} and new password {string}")
    public void a_change_password_request_with_old_and_new_password(String oldPassword, String newPassword) {
        request = new ChangePasswordRequest(oldPassword, newPassword);
    }

    @Given("the system cannot find user ID {long}")
    public void the_system_cannot_find_user_id(Long id) {
        doThrow(new EntityNotFoundException("User not found"))
                .when(userService).changePassword(eq(id), any());
    }

    @When("the user sends a PUT request to change the password for ID {long}")
    public void the_user_sends_a_put_request_to_change_the_password_for_id(Long id) throws Exception {
        ResultActions resultActions = mockMvc.perform(put("/api/v1/users/{id}/profile/password-change", id)
                .with(testContext.getSecurityProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)));
        testContext.setResultActions(resultActions);
    }

    @Then("the password change for ID {long} is processed by the user service")
    public void the_password_change_for_id__is_processed_by_the_user_service(Long id) {
        verify(userService, times(1))
                .changePassword(eq(id), any(ChangePasswordRequest.class));
    }
}
