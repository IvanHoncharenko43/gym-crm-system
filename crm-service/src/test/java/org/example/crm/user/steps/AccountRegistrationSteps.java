package org.example.crm.user.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.crm.trainee.controller.request.CreateTraineeRequest;
import org.example.crm.trainee.controller.response.TraineeSummary;
import org.example.crm.trainee.service.TraineeService;
import org.example.crm.trainer.controller.request.CreateTrainerRequest;
import org.example.crm.trainer.controller.response.TrainerSummary;
import org.example.crm.trainer.service.TrainerService;
import org.example.crm.trainingType.dto.TrainingType;
import org.example.crm.user.controller.dto.FullName;
import org.example.crm.user.controller.dto.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class AccountRegistrationSteps {

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

    private CreateTraineeRequest createTraineeRequest;
    private CreateTrainerRequest createTrainerRequest;

    @Given("a valid trainee registration request with all parameters")
    public void a_valid_trainee_registration_request_with_all_parameters() {
        createTraineeRequest = new CreateTraineeRequest(new FullName("John", "Doe"), LocalDate.of(2007, 3, 5), "Home");

        TraineeSummary traineeSummary = new TraineeSummary(1L, new UserProfile("John.Doe"), LocalDate.of(2007, 3, 5), "Home");
        when(traineeService.create(any(CreateTraineeRequest.class))).thenReturn(traineeSummary);
    }

    @Given("a valid trainee registration request with only required parameters")
    public void a_valid_trainee_registration_request_with_only_required_parameters() {
        createTraineeRequest = new CreateTraineeRequest(new FullName("John", "Doe"), null, null);

        TraineeSummary traineeSummary = new TraineeSummary(1L, new UserProfile("John.Doe"), null, null);
        when(traineeService.create(any(CreateTraineeRequest.class))).thenReturn(traineeSummary);
    }

    @Given("a trainee registration request with invalid {string}")
    public void a_trainee_registration_request_with_invalid(String fieldState) {
        switch (fieldState) {
            case "null full name" -> createTraineeRequest = new CreateTraineeRequest(null, LocalDate.of(2000, 1, 1), "Home");
            case "blank first name" -> createTraineeRequest = new CreateTraineeRequest(new FullName("  ", "Doe"), LocalDate.of(2000, 1, 1), "Home");
            case "blank last name" -> createTraineeRequest = new CreateTraineeRequest(new FullName("John", ""), LocalDate.of(2000, 1, 1), "Home");
            case "underage birth date" -> createTraineeRequest = new CreateTraineeRequest(new FullName("John", "Doe"), LocalDate.now().minusYears(10), "Home");
            case "address too long" -> createTraineeRequest = new CreateTraineeRequest(new FullName("John", "Doe"), LocalDate.of(2000, 1, 1), "A".repeat(201));
            default -> throw new IllegalArgumentException("Unknown field state mapping for: " + fieldState);
        }
    }

    @Given("a valid trainer registration request")
    public void a_valid_trainer_registration_request() {
        createTrainerRequest = new CreateTrainerRequest(new FullName("John", "Doe"), TrainingType.YOGA);

        TrainerSummary trainerSummary = new TrainerSummary(1L, new UserProfile("John.Doe"), TrainingType.YOGA);
        when(trainerService.create(any(CreateTrainerRequest.class))).thenReturn(trainerSummary);
    }

    @Given("a trainer registration request with invalid {string}")
    public void a_trainer_registration_request_with_invalid(String fieldState) {
        switch (fieldState) {
            case "null full name" -> createTrainerRequest = new CreateTrainerRequest(null, TrainingType.YOGA);
            case "blank first name" -> createTrainerRequest = new CreateTrainerRequest(new FullName("  ", "Doe"), TrainingType.YOGA);
            case "blank last name" -> createTrainerRequest = new CreateTrainerRequest(new FullName("John", ""), TrainingType.YOGA);
            case "null specialization" -> createTrainerRequest = new CreateTrainerRequest(new FullName("John", "Doe"), null);
            default -> throw new IllegalArgumentException("Unknown field state mapping for: " + fieldState);
        }
    }

    @When("the user sends a POST request to register a trainee")
    public void the_user_sends_a_post_request_to_register_a_trainee() throws Exception {
        ResultActions resultActions = mockMvc.perform(post("/api/v1/trainees")
                .with(testContext.getSecurityProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(createTraineeRequest)));
        testContext.setResultActions(resultActions);
    }

    @When("the user sends a POST request to register a trainer")
    public void the_user_sends_a_post_request_to_register_a_trainer() throws Exception {
        ResultActions resultActions = mockMvc.perform(post("/api/v1/trainers")
                .with(testContext.getSecurityProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(createTrainerRequest)));
        testContext.setResultActions(resultActions);
    }

    @Then("the trainee registration is processed by the trainee service")
    public void the_trainee_registration_is_processed() {
        verify(traineeService, times(1)).create(any(CreateTraineeRequest.class));
    }

    @Then("the trainer registration is processed by the trainer service")
    public void the_trainer_registration_is_processed() {
        verify(trainerService, times(1)).create(any(CreateTrainerRequest.class));
    }
}
