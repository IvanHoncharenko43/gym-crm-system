package org.example.workload.controller;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.example.workload.config.SecurityConfig;
import org.example.workload.controller.dto.ActionType;
import org.example.workload.controller.dto.FullName;
import org.example.workload.controller.dto.request.TrainerWorkloadRequest;
import org.example.workload.controller.dto.request.WorkloadQuery;
import org.example.workload.controller.dto.response.TrainerWorkloadSummary;
import org.example.workload.exception.InvalidStateTransitionException;
import org.example.workload.exception.WorkloadNotFoundException;
import org.example.workload.service.JwtService;
import org.example.workload.service.TrainerWorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.example.workload.TestUtils.*;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.Mockito.*;

@WebMvcTest(controllers = TrainerWorkloadController.class)
@Import(SecurityConfig.class)
class TrainerWorkloadControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainerWorkloadService trainerWorkloadService;

    @MockitoBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Test
    @WithMockUser(roles = {"TRAINER"})
    void updateWorkload_Return200_RequestIsValidAndUserIsTrainer() {
        TrainerWorkloadRequest request = getTrainerWorkloadRequest();

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post(TrainerWorkloadController.BASE_PATH)
                .then()
                .status(HttpStatus.OK);
        verify(trainerWorkloadService, times(1)).updateWorkload(request);
    }

    @Test
    @WithMockUser(roles = {"TRAINEE"})
    void updateWorkload_Return200_RequestIsValidAndUserIsTrainee() {
        TrainerWorkloadRequest request = getTrainerWorkloadRequest();

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post(TrainerWorkloadController.BASE_PATH)
                .then()
                .status(HttpStatus.OK);
        verify(trainerWorkloadService, times(1)).updateWorkload(request);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateWorkload_Return200_RequestIsValidAndUserIsAdmin() {
        TrainerWorkloadRequest request = getTrainerWorkloadRequest();

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post(TrainerWorkloadController.BASE_PATH)
                .then()
                .status(HttpStatus.OK);
        verify(trainerWorkloadService, times(1)).updateWorkload(request);
    }

    @Test
    @WithMockUser(roles = {"TRAINER"})
    void updateWorkload_Return400AndProblemDetail_UsernameIsBlank() {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                " ", new FullName(FIRST_NAME, LAST_NAME), true, TRAINING_DATE, DURATION_MINUTES, ActionType.ADD
        );

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post(TrainerWorkloadController.BASE_PATH)
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("title", equalTo("Bad Request"))
                .body("invalidFields", hasKey("username"));
    }

    @Test
    @WithMockUser(roles = {"TRAINER"})
    void updateWorkload_Return400AndProblemDetail_FullNameIsNull() {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                TRAINER_USERNAME, null, true, TRAINING_DATE, DURATION_MINUTES, ActionType.ADD
        );

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post(TrainerWorkloadController.BASE_PATH)
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("title", equalTo("Bad Request"))
                .body("invalidFields", hasKey("fullName"));
    }

    @Test
    @WithMockUser(roles = {"TRAINER"})
    void updateWorkload_Return400AndProblemDetail_TrainingDateIsNull() {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                TRAINER_USERNAME, new FullName(FIRST_NAME, LAST_NAME), true, null, DURATION_MINUTES, ActionType.ADD
        );

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post(TrainerWorkloadController.BASE_PATH)
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("title", equalTo("Bad Request"))
                .body("invalidFields", hasKey("trainingDate"));
    }

    @Test
    @WithMockUser(roles = {"TRAINER"})
    void updateWorkload_Return400AndProblemDetail_ActionTypeIsNull() {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                TRAINER_USERNAME, new FullName(FIRST_NAME, LAST_NAME), true, TRAINING_DATE, DURATION_MINUTES, null
        );

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post(TrainerWorkloadController.BASE_PATH)
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("title", equalTo("Bad Request"))
                .body("invalidFields", hasKey("actionType"));
    }

    @Test
    @WithMockUser(roles = {"TRAINER"})
    void updateWorkload_Return400AndProblemDetail_DurationBelowMinimum() {
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(ActionType.ADD, 10);

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post(TrainerWorkloadController.BASE_PATH)
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("title", equalTo("Bad Request"))
                .body("invalidFields", hasKey("trainingSummaryDurationMinutes"));
    }

    @Test
    @WithMockUser(roles = {"TRAINER"})
    void updateWorkload_Return400AndProblemDetail_DurationAboveMaximum() {
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(ActionType.ADD, 400);

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post(TrainerWorkloadController.BASE_PATH)
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("title", equalTo("Bad Request"))
                .body("invalidFields", hasKey("trainingSummaryDurationMinutes"));
    }

    @Test
    @WithAnonymousUser
    void updateWorkload_Return401AndProblemDetail_UserIsUnauthenticated() {
        TrainerWorkloadRequest request = getTrainerWorkloadRequest();

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post(TrainerWorkloadController.BASE_PATH)
                .then()
                .status(HttpStatus.UNAUTHORIZED)
                .body("title", equalTo("Authentication Failure"))
                .body("detail", equalTo("Authentication token is missing or invalid"));
        verify(trainerWorkloadService, never()).updateWorkload(any());
    }

    @Test
    @WithMockUser
    void updateWorkload_Return403AndProblemDetail_UserRoleIsNotSupported() {
        TrainerWorkloadRequest request = getTrainerWorkloadRequest();

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post(TrainerWorkloadController.BASE_PATH)
                .then()
                .status(HttpStatus.FORBIDDEN)
                .body("title", equalTo("Authorization Failure"))
                .body("detail", equalTo("Not enough rights to access the resource"));
        verify(trainerWorkloadService, never()).updateWorkload(any());
    }

    @Test
    @WithMockUser(roles = {"TRAINER"})
    void updateWorkload_Return404AndProblemDetail_WorkloadNotFound() {
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(ActionType.DELETE, DURATION_MINUTES);
        doThrow(new WorkloadNotFoundException("Cannot update-delete trainer's workload because there isn't one to alter"))
                .when(trainerWorkloadService).updateWorkload(request);

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post(TrainerWorkloadController.BASE_PATH)
                .then()
                .status(HttpStatus.NOT_FOUND)
                .body("title", equalTo("Workload Not Found"))
                .body("detail", equalTo("Cannot update-delete trainer's workload because there isn't one to alter"));
        verify(trainerWorkloadService, times(1)).updateWorkload(request);

    }

    @Test
    @WithMockUser(roles = {"TRAINER"})
    void updateWorkload_Return409AndProblemDetail_InvalidStateTransition() {
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(ActionType.DELETE, DURATION_MINUTES);
        doThrow(new InvalidStateTransitionException("Cannot update trainer's workload because resulting workload cannot go negative"))
                .when(trainerWorkloadService).updateWorkload(request);

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post(TrainerWorkloadController.BASE_PATH)
                .then()
                .status(HttpStatus.CONFLICT)
                .body("title", equalTo("Invalid State Transition"))
                .body("detail", equalTo("Cannot update trainer's workload because resulting workload cannot go negative"));
        verify(trainerWorkloadService, times(1)).updateWorkload(request);
    }

    @Test
    @WithMockUser(roles = {"TRAINER"})
    void getWorkload_Return200AndSummary_RequestIsValidAndUserIsTrainer() {
        WorkloadQuery query = getWorkloadQuery();
        TrainerWorkloadSummary expectedResult = getTrainerWorkloadSummary(query.year(), query.month(), DURATION_MINUTES);
        when(trainerWorkloadService.getMonthlySummary(query)).thenReturn(expectedResult);

        TrainerWorkloadSummary result = given()
                .queryParam("username", query.username())
                .queryParam("year", query.year())
                .queryParam("month", query.month())
                .when()
                .get(TrainerWorkloadController.BASE_PATH)
                .then()
                .status(HttpStatus.OK)
                .extract()
                .as(TrainerWorkloadSummary.class);
        assertThat(result).isEqualTo(expectedResult);
        verify(trainerWorkloadService, times(1)).getMonthlySummary(query);
    }

    @Test
    @WithMockUser(roles = {"TRAINEE"})
    void getWorkload_Return200AndSummary_RequestIsValidAndUserIsTrainee() {
        WorkloadQuery query = getWorkloadQuery();
        TrainerWorkloadSummary expectedResult = getTrainerWorkloadSummary(query.year(), query.month(), DURATION_MINUTES);
        when(trainerWorkloadService.getMonthlySummary(query)).thenReturn(expectedResult);

        TrainerWorkloadSummary result = given()
                .queryParam("username", query.username())
                .queryParam("year", query.year())
                .queryParam("month", query.month())
                .when()
                .get(TrainerWorkloadController.BASE_PATH)
                .then()
                .status(HttpStatus.OK)
                .extract()
                .as(TrainerWorkloadSummary.class);
        assertThat(result).isEqualTo(expectedResult);
        verify(trainerWorkloadService, times(1)).getMonthlySummary(query);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getWorkload_Return200AndSummary_RequestIsValidAndUserIsAdmin() {
        WorkloadQuery query = getWorkloadQuery();
        TrainerWorkloadSummary expectedResult = getTrainerWorkloadSummary(query.year(), query.month(), DURATION_MINUTES);
        when(trainerWorkloadService.getMonthlySummary(query)).thenReturn(expectedResult);

        TrainerWorkloadSummary result = given()
                .queryParam("username", query.username())
                .queryParam("year", query.year())
                .queryParam("month", query.month())
                .when()
                .get(TrainerWorkloadController.BASE_PATH)
                .then()
                .status(HttpStatus.OK)
                .extract()
                .as(TrainerWorkloadSummary.class);
        assertThat(result).isEqualTo(expectedResult);
        verify(trainerWorkloadService, times(1)).getMonthlySummary(query);
    }

    @Test
    @WithMockUser(roles = {"TRAINER"})
    void getWorkload_Return400AndProblemDetail_UsernameIsBlank() {
        given()
                .queryParam("username", "  ")
                .queryParam("year", TRAINING_DATE.getYear())
                .queryParam("month", TRAINING_DATE.getMonthValue())
                .when()
                .get(TrainerWorkloadController.BASE_PATH)
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("title", equalTo("Bad Request"))
                .body("invalidFields", hasKey("username"));
    }

    @Test
    @WithMockUser(roles = {"TRAINER"})
    void getWorkload_Return400AndProblemDetail_YearBelowMinimum() {
        given()
                .queryParam("username", TRAINER_USERNAME)
                .queryParam("year", 1999)
                .queryParam("month", TRAINING_DATE.getMonthValue())
                .when()
                .get(TrainerWorkloadController.BASE_PATH)
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("title", equalTo("Bad Request"))
                .body("invalidFields", hasKey("year"));
    }

    @Test
    @WithMockUser(roles = {"TRAINER"})
    void getWorkload_Return400AndProblemDetail_MonthBelowMinimum() {
        given()
                .queryParam("username", TRAINER_USERNAME)
                .queryParam("year", TRAINING_DATE.getYear())
                .queryParam("month", 0)
                .when()
                .get(TrainerWorkloadController.BASE_PATH)
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("title", equalTo("Bad Request"))
                .body("invalidFields", hasKey("month"));
    }

    @Test
    @WithMockUser(roles = {"TRAINER"})
    void getWorkload_Return400AndProblemDetail_MonthAboveMaximum() {
        given()
                .queryParam("username", TRAINER_USERNAME)
                .queryParam("year", TRAINING_DATE.getYear())
                .queryParam("month", 13)
                .when()
                .get(TrainerWorkloadController.BASE_PATH)
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("title", equalTo("Bad Request"))
                .body("invalidFields", hasKey("month"));
    }

    @Test
    @WithAnonymousUser
    void getWorkload_Return401AndProblemDetail_UserIsUnauthenticated() {
        WorkloadQuery query = getWorkloadQuery();

        given()
                .queryParam("username", query.username())
                .queryParam("year", query.year())
                .queryParam("month", query.month())
                .when()
                .get(TrainerWorkloadController.BASE_PATH)
                .then()
                .status(HttpStatus.UNAUTHORIZED)
                .body("title", equalTo("Authentication Failure"))
                .body("detail", equalTo("Authentication token is missing or invalid"));
        verify(trainerWorkloadService, never()).getMonthlySummary(any());
    }

    @Test
    @WithMockUser
    void getWorkload_Return403AndProblemDetail_UserRoleIsNotSupported() {
        WorkloadQuery query = getWorkloadQuery();

        given()
                .queryParam("username", query.username())
                .queryParam("year", query.year())
                .queryParam("month", query.month())
                .when()
                .get(TrainerWorkloadController.BASE_PATH)
                .then()
                .status(HttpStatus.FORBIDDEN)
                .body("title", equalTo("Authorization Failure"))
                .body("detail", equalTo("Not enough rights to access the resource"));
        verify(trainerWorkloadService, never()).getMonthlySummary(any());
    }

    @Test
    @WithMockUser(roles = {"TRAINER"})
    void getWorkload_Return404AndProblemDetail_WorkloadNotFound() {
        WorkloadQuery query = getWorkloadQuery();
        when(trainerWorkloadService.getMonthlySummary(query))
                .thenThrow(new WorkloadNotFoundException("No workload found for trainer"));

        given()
                .queryParam("username", query.username())
                .queryParam("year", query.year())
                .queryParam("month", query.month())
                .when()
                .get(TrainerWorkloadController.BASE_PATH)
                .then()
                .status(HttpStatus.NOT_FOUND)
                .body("title", equalTo("Workload Not Found"))
                .body("detail", equalTo("No workload found for trainer"));
        verify(trainerWorkloadService, times(1)).getMonthlySummary(query);
    }
}
