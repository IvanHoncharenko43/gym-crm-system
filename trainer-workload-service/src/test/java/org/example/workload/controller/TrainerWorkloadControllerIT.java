package org.example.workload.controller;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.example.workload.config.SecurityConfig;
import org.example.workload.controller.dto.request.WorkloadQuery;
import org.example.workload.controller.dto.response.TrainerWorkloadSummary;
import org.example.workload.exception.WorkloadNotFoundException;
import org.example.workload.service.JwtService;
import org.example.workload.service.TrainerWorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
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
