package org.example.crm.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.crm.cucumber.config.TestContext;
import org.example.crm.exception.EntityNotFoundException;
import org.example.crm.trainer.controller.request.UpdateTrainerRequest;
import org.example.crm.trainer.controller.response.TrainerSummary;
import org.example.crm.trainer.service.TrainerService;
import org.example.crm.trainingType.dto.TrainingType;
import org.example.crm.user.controller.dto.FullName;
import org.example.crm.user.controller.dto.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class TrainerProfileManagementSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private TestContext testContext;

    private UpdateTrainerRequest updateRequest;

    @Given("a valid trainer profile exists for ID {long}")
    public void a_valid_trainer_profile_exists_for_id(Long id) {
        TrainerSummary trainerSummary = new TrainerSummary(id, new UserProfile("John.Doe"), TrainingType.YOGA);
        when(trainerService.getById(eq(id))).thenReturn(trainerSummary);
    }

    @Given("the system cannot find trainer ID {long}")
    public void the_system_cannot_find_trainer_id(Long id) {
        doThrow(new EntityNotFoundException("Trainer not found"))
                .when(trainerService).getById(eq(id));
        doThrow(new EntityNotFoundException("Trainer not found"))
                .when(trainerService).update(eq(id), any());
        doThrow(new EntityNotFoundException("Trainer not found"))
                .when(trainerService).changeActivity(eq(id));
    }

    @Given("a valid update trainer profile request")
    public void a_valid_update_trainer_profile_request() {
        updateRequest = new UpdateTrainerRequest(new FullName("John", "Doe"), TrainingType.YOGA);
        TrainerSummary trainerSummary = new TrainerSummary(1L, new UserProfile("John.Doe"), TrainingType.YOGA);
        when(trainerService.update(anyLong(), any())).thenReturn(trainerSummary);
    }

    @Given("an update trainer profile request with invalid {string}")
    public void an_update_trainer_profile_request_with_invalid(String fieldState) {
        switch (fieldState) {
            case "null full name" -> updateRequest = new UpdateTrainerRequest(null, TrainingType.YOGA);
            case "blank first name" -> updateRequest = new UpdateTrainerRequest(new FullName("", "Doe"), TrainingType.YOGA);
            case "blank last name" -> updateRequest = new UpdateTrainerRequest(new FullName("John", ""), TrainingType.YOGA);
            case "null specialization" -> updateRequest = new UpdateTrainerRequest(new FullName("John", "Doe"), null);
            default -> throw new IllegalArgumentException("Unknown field state mapping for: " + fieldState);
        }
    }

    @When("the user sends a GET request to retrieve a trainer profile information for ID {long}")
    public void the_user_sends_a_get_request_to_retrieve_a_trainer_profile(Long id) throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/v1/trainers/{id}", id)
                .with(testContext.getSecurityProcessor()));
        testContext.setResultActions(resultActions);
    }

    @When("the user sends a PUT request to update the trainer profile for ID {long}")
    public void the_user_sends_a_put_request_to_update_the_trainer(Long id) throws Exception {
        ResultActions resultActions = mockMvc.perform(put("/api/v1/trainers/{id}", id)
                .with(testContext.getSecurityProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(updateRequest)));
        testContext.setResultActions(resultActions);
    }

    @When("the user sends a PATCH request to change trainer activity status for ID {long}")
    public void the_user_sends_a_patch_request(Long id) throws Exception {
        ResultActions resultActions = mockMvc.perform(patch("/api/v1/trainers/{id}/profile/active-status/change", id)
                .with(testContext.getSecurityProcessor()));
        testContext.setResultActions(resultActions);
    }

    @Then("the trainer summary is returned")
    public void the_trainer_summary_is_returned() throws Exception {
        testContext.getResultActions().andExpect(jsonPath("$.id").exists());
    }

    @Then("the trainer retrieval for ID {long} is processed by the trainer service")
    public void the_trainer_retrieval_is_processed(Long id) {
        verify(trainerService, times(1)).getById(eq(id));
    }

    @Then("the update for ID {long} is processed by the trainer service")
    public void the_update_is_processed(Long id) {
        verify(trainerService, times(1)).update(eq(id), any(UpdateTrainerRequest.class));
    }

    @Then("the activity status change for ID {long} is processed by the trainer service")
    public void the_activity_status_change_is_processed(Long id) {
        verify(trainerService, times(1)).changeActivity(eq(id));
    }
}
