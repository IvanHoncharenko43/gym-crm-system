package org.example.crm.user.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.crm.exception.AccessForbiddenException;
import org.example.crm.exception.EntityNotFoundException;
import org.example.crm.trainee.controller.request.CreateTraineeRequest;
import org.example.crm.trainee.controller.request.UpdateTraineeTrainersRequest;
import org.example.crm.trainee.controller.response.TraineeSummary;
import org.example.crm.trainee.service.TraineeService;
import org.example.crm.trainer.controller.request.CreateTrainerRequest;
import org.example.crm.trainer.controller.response.TrainerSummary;
import org.example.crm.trainer.controller.response.Trainers;
import org.example.crm.trainer.service.TrainerService;
import org.example.crm.trainingType.dto.TrainingType;
import org.example.crm.user.controller.dto.FullName;
import org.example.crm.user.controller.dto.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class TrainerAssignmentSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    private TraineeService traineeService;

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private TestContext testContext;

    private UpdateTraineeTrainersRequest updateRequest;

    @Given("the system cannot find trainee ID {long} when updating trainers")
    public void the_system_cannot_find_trainee_id_when_updating_trainers(Long id) {
        doThrow(new EntityNotFoundException("Trainee not found"))
                .when(traineeService).updateTrainersList(eq(id), any(UpdateTraineeTrainersRequest.class));
    }

    @Given("a valid update trainee trainers request")
    public void a_valid_update_trainee_trainers_request() {
        updateRequest = new UpdateTraineeTrainersRequest(List.of("Trainer.Name"));

        Trainers trainers = new Trainers(List.of(
                new TrainerSummary(2L, new UserProfile("Trainer.Name"), TrainingType.YOGA)
        ));
        doReturn(trainers).when(traineeService).updateTrainersList(anyLong(), any(UpdateTraineeTrainersRequest.class));
    }

    @Given("an update trainee trainers request with invalid {string}")
    public void an_update_trainee_trainers_request_with_invalid(String fieldState) {
        switch (fieldState) {
            case "empty list" -> updateRequest = new UpdateTraineeTrainersRequest(List.of());
            case "blank username in list" -> updateRequest = new UpdateTraineeTrainersRequest(List.of("John.Doe", "  ", "John.Doe1"));
            default -> throw new IllegalArgumentException("Unknown field state mapping for: " + fieldState);
        }
    }

    @When("the user sends a GET request to retrieve not assigned trainers for username {string}")
    public void the_user_sends_a_get_request_to_retrieve_not_assigned_trainers(String username) throws Exception {
        Trainers mockTrainers = new Trainers(List.of(
                new TrainerSummary(2L, new UserProfile("Trainer.Name"), TrainingType.YOGA)
        ));
        doReturn(mockTrainers).when(trainerService).getUnassignedTrainersByTraineeList(eq(username));

        ResultActions resultActions = mockMvc.perform(get("/api/v1/trainers/not-assigned")
                .param("trainee-username", username)
                .with(testContext.getSecurityProcessor()));
        testContext.setResultActions(resultActions);
    }

    @When("the user sends a PUT request to update the trainers list for ID {long}")
    public void the_user_sends_a_put_request_to_update_the_trainers_list(Long id) throws Exception {
        ResultActions resultActions = mockMvc.perform(put("/api/v1/trainees/{id}/trainers-update", id)
                .with(testContext.getSecurityProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(updateRequest)));
        testContext.setResultActions(resultActions);
    }

    @Then("the trainers list is returned")
    public void the_trainers_list_is_returned() throws Exception {
        testContext.getResultActions()
                .andExpect(jsonPath("$.trainers").exists())
                .andExpect(jsonPath("$.trainers").isArray());
    }

    @Then("the updated trainers list is returned")
    public void the_updated_trainers_list_is_returned() throws Exception {
        testContext.getResultActions()
                .andExpect(jsonPath("$.trainers").exists())
                .andExpect(jsonPath("$.trainers").isArray());
    }

    @Then("the unassigned trainers retrieval for {string} is processed by the trainer service")
    public void the_unassigned_trainers_retrieval_is_processed(String username) {
        verify(trainerService, times(1)).getUnassignedTrainersByTraineeList(eq(username));
    }

    @Then("the trainers list update for ID {long} is processed by the trainee service")
    public void the_trainers_list_update_is_processed(Long id) {
        verify(traineeService, times(1)).updateTrainersList(eq(id), any(UpdateTraineeTrainersRequest.class));
    }
}
