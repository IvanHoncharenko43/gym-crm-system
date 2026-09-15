package org.example.crm.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.crm.cucumber.config.TestContext;
import org.example.crm.exception.EntityNotFoundException;
import org.example.crm.trainee.controller.request.UpdateTraineeRequest;
import org.example.crm.trainee.controller.response.TraineeSummary;
import org.example.crm.trainee.service.TraineeService;
import org.example.crm.user.controller.dto.FullName;
import org.example.crm.user.controller.dto.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class TraineeProfileManagementSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    private TraineeService traineeService;

    @Autowired
    private TestContext testContext;
    private UpdateTraineeRequest updateRequest;

    @Given("a valid trainee profile exists for ID {long}")
    public void a_valid_trainer_profile_exists_for_id(Long id) {
        TraineeSummary traineeSummary = new TraineeSummary(id, new UserProfile("John.Doe"), LocalDate.of(2000, 1, 1), "Home");
        when(traineeService.getById(eq(id))).thenReturn(traineeSummary);
    }

    @Given("the system cannot find trainee ID {long}")
    public void the_system_cannot_find_user_id(Long id) {
        doThrow(new EntityNotFoundException("Trainee not found"))
                .when(traineeService).getById(eq(id));
        doThrow(new EntityNotFoundException("Trainee not found"))
                .when(traineeService).update(eq(id), any());
        doThrow(new EntityNotFoundException("Trainee not found"))
                .when(traineeService).changeActivity(eq(id));
    }

    @Given("a valid update trainee profile request")
    public void a_valid_update_trainee_profile_request() {
        updateRequest = new UpdateTraineeRequest(new FullName("John", "Doe"), LocalDate.of(2000, 1, 1), "Home");
        TraineeSummary traineeSummary = new TraineeSummary(1L, new UserProfile("John.Doe"), LocalDate.of(2000, 1, 1), "Home");
        when(traineeService.update(anyLong(), any())).thenReturn(traineeSummary);
    }

    @Given("an update trainee profile request with invalid {string}")
    public void an_update_trainee_profile_request_with_invalid(String fieldState) {
        switch (fieldState) {
            case "null full name" -> updateRequest = new UpdateTraineeRequest(null, LocalDate.of(2000, 1, 1), "Home");
            case "blank first name" -> updateRequest = new UpdateTraineeRequest(new FullName("", "Doe"), LocalDate.of(2000, 1, 1), "Home");
            case "blank last name" -> updateRequest = new UpdateTraineeRequest(new FullName("John", ""), LocalDate.of(2000, 1, 1), "Home");
            case "underage birth date" -> updateRequest = new UpdateTraineeRequest(new FullName("John", "Doe"), LocalDate.now().minusYears(10), "Home");
            case "address too long" -> updateRequest = new UpdateTraineeRequest(new FullName("John", "Doe"), LocalDate.of(2000, 1, 1), "A".repeat(201));
            default -> throw new IllegalArgumentException("Unknown field state mapping for: " + fieldState);
        }
    }

    @When("the user sends a GET request to retrieve a trainee profile information for ID {long}")
    public void the_user_sends_a_get_request_to_retrieve_a_profile(Long id) throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/v1/trainees/{id}", id)
                .with(testContext.getSecurityProcessor()));
        testContext.setResultActions(resultActions);
    }

    @When("the user sends a PUT request to update the trainee profile for ID {long}")
    public void the_user_sends_a_put_request_to_update_the_trainee(Long id) throws Exception {
        ResultActions resultActions = mockMvc.perform(put("/api/v1/trainees/{id}", id)
                .with(testContext.getSecurityProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(updateRequest)));
        testContext.setResultActions(resultActions);
    }

    @When("the user sends a DELETE request to delete the trainee profile for username {string}")
    public void the_user_sends_a_delete_request(String username) throws Exception {
        ResultActions resultActions = mockMvc.perform(delete("/api/v1/trainees")
                .param("username", username)
                .with(testContext.getSecurityProcessor()));
        testContext.setResultActions(resultActions);
    }

    @When("the user sends a PATCH request to change trainee activity status for ID {long}")
    public void the_user_sends_a_patch_request(Long id) throws Exception {
        ResultActions resultActions = mockMvc.perform(patch("/api/v1/trainees/{id}/profile/active-status/change", id)
                .with(testContext.getSecurityProcessor()));
        testContext.setResultActions(resultActions);
    }

    @Then("the trainee summary is returned")
    public void the_trainee_summary_is_returned() throws Exception {
        testContext.getResultActions().andExpect(jsonPath("$.id").exists());
    }

    @Then("the trainee retrieval for ID {long} is processed by the trainee service")
    public void the_trainee_retrieval_is_processed(Long id) {
        verify(traineeService, times(1)).getById(eq(id));
    }

    @Then("the update for ID {long} is processed by the trainee service")
    public void the_update_is_processed(Long id) {
        verify(traineeService, times(1)).update(eq(id), any(UpdateTraineeRequest.class));
    }

    @Then("the deletion for username {string} is processed by the trainee service")
    public void the_deletion_is_processed(String username) {
        verify(traineeService, times(1)).deleteByUsername(eq(username));
    }

    @Then("the activity status change for ID {long} is processed by the trainee service")
    public void the_activity_status_change_is_processed(Long id) {
        verify(traineeService, times(1)).changeActivity(eq(id));
    }
}
