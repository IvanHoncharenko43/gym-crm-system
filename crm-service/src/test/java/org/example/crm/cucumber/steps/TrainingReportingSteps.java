package org.example.crm.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.crm.TestUtils;
import org.example.crm.cucumber.config.TestContext;
import org.example.crm.trainee.controller.request.GetTraineeTrainingsRequest;
import org.example.crm.trainee.controller.response.TraineeSummary;
import org.example.crm.trainer.controller.request.GetTrainerTrainingsRequest;
import org.example.crm.trainer.controller.response.TrainerSummary;
import org.example.crm.trainer.controller.response.TrainerWorkloadSummary;
import org.example.crm.trainer.service.TrainerWorkloadService;
import org.example.crm.training.controller.response.TrainingSummary;
import org.example.crm.training.controller.response.Trainings;
import org.example.crm.training.service.TrainingService;
import org.example.crm.trainingType.dto.TrainingType;
import org.example.crm.user.controller.dto.FullName;
import org.example.crm.user.controller.dto.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class TrainingReportingSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    private TrainingService trainingService;

    @Autowired
    private TrainerWorkloadService trainerWorkloadService;

    @Autowired
    private TestContext testContext;

    private GetTrainerTrainingsRequest trainerSearchRequest;
    private GetTraineeTrainingsRequest traineeSearchRequest;

    @Given("a valid trainer training search request with all parameters")
    public void a_valid_trainer_training_search_request_with_all_parameters() {
        trainerSearchRequest = new GetTrainerTrainingsRequest(
                "Trainer.Name", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "John.Doe"
        );
    }

    @Given("a valid trainer training search request with only required parameters")
    public void a_valid_trainer_training_search_request_with_only_required_parameters() {
        trainerSearchRequest = new GetTrainerTrainingsRequest(
                "Trainer.Name", null, null, null);
    }

    @Given("a trainer training search request with invalid {string}")
    public void a_trainer_training_search_request_with_invalid(String fieldState) {
        switch (fieldState) {
            case "blank username" -> trainerSearchRequest = new GetTrainerTrainingsRequest("  ", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "John.Doe");
            case "inverted date range" -> trainerSearchRequest = new GetTrainerTrainingsRequest("Trainer.Name", LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1), "John.Doe");
            case "trainee name too long" -> trainerSearchRequest = new GetTrainerTrainingsRequest("Trainer.Name", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "A".repeat(51));
            default -> throw new IllegalArgumentException("Unknown field state mapping for: " + fieldState);
        }
    }

    @Given("valid trainer training records exist for {string}")
    public void valid_trainer_training_records_exist(String username) {
        TraineeSummary traineeSummary = new TraineeSummary(1L, new UserProfile("John.Doe"), LocalDate.of(2007, 3, 25), "Home 21 Street");
        TrainerSummary trainerSummary = new TrainerSummary(1L, new UserProfile("John.Doe1"), TrainingType.YOGA);
        TrainingSummary trainingSummary = new TrainingSummary(1L, trainerSummary, traineeSummary, "Training name", TrainingType.YOGA, LocalDate.of(2026, 12, 31), 60);
        Trainings trainings = new Trainings(List.of(trainingSummary));
        when(trainingService.getTrainerTrainingList(any(GetTrainerTrainingsRequest.class))).thenReturn(trainings);
    }

    @Given("a valid trainee training search request with all parameters")
    public void a_valid_trainee_training_search_request_with_all_parameters() {
        traineeSearchRequest = new GetTraineeTrainingsRequest(
                "John.Doe", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "Trainer.Name", TrainingType.YOGA
        );
    }

    @Given("a valid trainee training search request with only required parameters")
    public void a_valid_trainee_training_search_request_with_only_required_parameters() {
        traineeSearchRequest = new GetTraineeTrainingsRequest(
                "John.Doe", null, null, null, null
        );
    }

    @Given("a trainee training search request with invalid {string}")
    public void a_trainee_training_search_request_with_invalid(String fieldState) {
        switch (fieldState) {
            case "blank username" -> traineeSearchRequest = new GetTraineeTrainingsRequest("  ", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "Trainer.Name", TrainingType.YOGA);
            case "inverted date range" -> traineeSearchRequest = new GetTraineeTrainingsRequest("John.Doe", LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1), "Trainer.Name", TrainingType.YOGA);
            case "trainer name too long" -> traineeSearchRequest = new GetTraineeTrainingsRequest("John.Doe", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "A".repeat(51), TrainingType.YOGA);
            default -> throw new IllegalArgumentException("Unknown field state mapping for: " + fieldState);
        }
    }

    @Given("valid trainee training records exist for {string}")
    public void valid_trainee_training_records_exist(String username) {
        TraineeSummary traineeSummary = new TraineeSummary(1L, new UserProfile("John.Doe"), LocalDate.of(2007, 3, 25), "Home 21 Street");
        TrainerSummary trainerSummary = new TrainerSummary(1L, new UserProfile("John.Doe1"), TrainingType.YOGA);
        TrainingSummary trainingSummary = new TrainingSummary(1L, trainerSummary, traineeSummary, "Training name", TrainingType.YOGA, LocalDate.of(2026, 12, 31), 60);
        Trainings trainings = new Trainings(List.of(trainingSummary));
        when(trainingService.getTraineeTrainingList(any(GetTraineeTrainingsRequest.class))).thenReturn(trainings);
    }

    @Given("a valid trainer workload summary exists for {string}")
    public void a_valid_trainer_workload_summary_exists(String username) {
        TrainerWorkloadSummary trainerWorkloadSummary = new TrainerWorkloadSummary(username, new FullName("Trainer", "Name"), true, 2026, 5, 600);
        when(trainerWorkloadService.getWorkload(any())).thenReturn(trainerWorkloadSummary);
    }

    @When("the user sends a POST request to search trainer trainings")
    public void the_user_sends_a_post_request_to_search_trainer_trainings() throws Exception {
        ResultActions resultActions = mockMvc.perform(post("/api/v1/trainers/trainings/search")
                .with(testContext.getSecurityProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(trainerSearchRequest)));
        testContext.setResultActions(resultActions);
    }

    @When("the user sends a POST request to search trainee trainings")
    public void the_user_sends_a_post_request_to_search_trainee_trainings() throws Exception {
        ResultActions resultActions = mockMvc.perform(post("/api/v1/trainees/trainings/search")
                .with(testContext.getSecurityProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(traineeSearchRequest)));
        testContext.setResultActions(resultActions);
    }

    @When("the user sends a GET request to retrieve the monthly workload with valid parameters")
    public void the_user_sends_a_get_request_to_retrieve_workload_valid() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/v1/trainers/workloads")
                .with(testContext.getSecurityProcessor())
                .param("username", "Trainer.Name")
                .param("year", "2026")
                .param("month", "5"));
        testContext.setResultActions(resultActions);
    }

    @When("the user sends a GET request to retrieve the monthly workload with invalid {string}")
    public void the_user_sends_a_get_request_to_retrieve_workload_invalid(String fieldState) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get("/api/v1/trainers/workloads").with(testContext.getSecurityProcessor());
        switch (fieldState) {
            case "blank username" -> requestBuilder.param("username", "  ").param("year", "2026").param("month", "5");
            case "negative year" -> requestBuilder.param("username", "Trainer.Name").param("year", "-5").param("month", "5");
            case "month out of bounds (13)" -> requestBuilder.param("username", "Trainer.Name").param("year", "2026").param("month", "13");
            default -> throw new IllegalArgumentException("Unknown field state mapping for: " + fieldState);
        }
        testContext.setResultActions(mockMvc.perform(requestBuilder));
    }

    @Then("the trainings list is returned")
    public void the_trainings_list_is_returned() throws Exception {
        testContext.getResultActions()
                .andExpect(jsonPath("$.trainings").exists())
                .andExpect(jsonPath("$.trainings").isArray());
    }

    @Then("the trainer workload summary is returned")
    public void the_trainer_workload_summary_is_returned() throws Exception {
        testContext.getResultActions()
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.trainingSummaryDurationMinutes").exists());
    }

    @Then("the trainer training search is processed by the training service")
    public void the_trainer_training_search_is_processed() {
        verify(trainingService, times(1)).getTrainerTrainingList(any(GetTrainerTrainingsRequest.class));
    }

    @Then("the trainee training search is processed by the training service")
    public void the_trainee_training_search_is_processed() {
        verify(trainingService, times(1)).getTraineeTrainingList(any(GetTraineeTrainingsRequest.class));
    }

    @Then("the workload retrieval is processed by the trainer workload service")
    public void the_workload_retrieval_is_processed() {
        verify(trainerWorkloadService, times(1)).getWorkload(any());
    }
}
