package org.example.training;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.example.TestUtils;
import org.example.config.SecurityConfig;
import org.example.security.service.JwtService;
import org.example.security.service.TokenBlackListService;
import org.example.training.controller.TrainingController;
import org.example.training.controller.request.CreateTrainingRequest;
import org.example.training.controller.response.TrainingSummary;
import org.example.training.service.TrainingService;
import org.example.trainingType.dto.TrainingType;
import org.example.trainingType.dto.TrainingTypes;
import org.example.trainingType.service.TrainingTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.example.TestUtils.*;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.Mockito.*;

@WebMvcTest(controllers = TrainingController.class)
@Import(SecurityConfig.class)
class TrainingControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainingService trainingService;

    @MockitoBean
    private TrainingTypeService trainingTypeService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private TokenBlackListService tokenBlackListService;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void addTraining_Return200AndTrainingSummary_RequestIsValid() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TRAINER_USERNAME, TRAINEE_USERNAME, "Morning Cardio",
                LocalDate.now().plusDays(7), 45
        );
        TrainingSummary expectedResult = getTrainingSummary();
        when(trainingService.create(request)).thenReturn(expectedResult);

        TrainingSummary result = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post("/api/v1/trainings")
                .then()
                .status(HttpStatus.OK)
                .extract()
                .as(TrainingSummary.class);

        assertThat(result).isNotNull();
        assertThat(result.trainingName()).isEqualTo(expectedResult.trainingName());
        assertThat(result.trainingDate()).isEqualTo(expectedResult.trainingDate());
        assertThat(result.durationMinutes()).isEqualTo(expectedResult.durationMinutes());
        assertThat(result.trainingType()).isEqualTo(expectedResult.trainingType());
        assertThat(result.trainee()).isEqualTo(expectedResult.trainee());
        assertThat(result.trainer()).isEqualTo(expectedResult.trainer());
        verify(trainingService, times(1)).create(request);
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void addTraining_Return400AndProblemDetail_TrainerUsernameIsBlank() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                "  ", TRAINEE_USERNAME, "Cardio", LocalDate.now().plusDays(2), 45
        );

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post("/api/v1/trainings")
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("title", equalTo("Bad Request"))
                .body("invalidFields", hasKey("trainerUsername"));
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void addTraining_Return400AndProblemDetail_TraineeUsernameIsBlank() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TRAINER_USERNAME, "", "Cardio", LocalDate.now().plusDays(2), 45
        );

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post("/api/v1/trainings")
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("title", equalTo("Bad Request"))
                .body("invalidFields", hasKey("traineeUsername"));
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void addTraining_Return400AndProblemDetail_TrainingNameIsBlank() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TRAINER_USERNAME, TRAINEE_USERNAME, "", LocalDate.now().plusDays(2), 45
        );

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post("/api/v1/trainings")
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("title", equalTo("Bad Request"))
                .body("invalidFields", hasKey("trainingName"));
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void addTraining_Return400AndProblemDetail_TrainingDateIsNull() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TRAINER_USERNAME, TRAINEE_USERNAME, "Cardio", null, 45
        );

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post("/api/v1/trainings")
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("title", equalTo("Bad Request"))
                .body("invalidFields", hasKey("trainingDate"));
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void addTraining_Return400AndProblemDetail_TrainingDateIsNotInValidAdvance() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TestUtils.TRAINER_USERNAME, TestUtils.TRAINEE_USERNAME, "Cardio", LocalDate.now(), 45
        );

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post("/api/v1/trainings")
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("title", equalTo("Bad Request"))
                .body("invalidFields", hasKey("trainingDate"));
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void addTraining_Return400AndProblemDetail_DurationIsNegative() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TestUtils.TRAINER_USERNAME, TestUtils.TRAINEE_USERNAME,
                "Cardio", LocalDate.now().plusDays(2), -10
        );

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post("/api/v1/trainings")
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("title", equalTo("Bad Request"))
                .body("invalidFields", hasKey("durationMinutes"));
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void addTraining_Return400AndProblemDetail_DurationBelowMinimum() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TestUtils.TRAINER_USERNAME, TestUtils.TRAINEE_USERNAME,
                "Cardio", LocalDate.now().plusDays(2), 10
        );

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post("/api/v1/trainings")
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("title", equalTo("Bad Request"))
                .body("invalidFields", hasKey("durationMinutes"));
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void addTraining_Return400AndProblemDetail_DurationAboveMaximum() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TestUtils.TRAINER_USERNAME, TestUtils.TRAINEE_USERNAME,
                "Cardio", LocalDate.now().plusDays(2), 400
        );

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post("/api/v1/trainings")
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("title", equalTo("Bad Request"))
                .body("invalidFields", hasKey("durationMinutes"));
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void getTrainingTypes_Returns200AndTrainingTypes() {
        TrainingTypes types = new TrainingTypes(List.of(TrainingType.YOGA, TrainingType.CARDIO));
        when(trainingTypeService.getAllTrainingTypes()).thenReturn(types);

        TrainingTypes result = given()
                .when()
                .get("/api/v1/trainings/training-types")
                .then()
                .status(HttpStatus.OK)
                .extract()
                .as(TrainingTypes.class);

        assertThat(result.trainingTypes()).containsExactly(TrainingType.YOGA, TrainingType.CARDIO);
        verify(trainingTypeService, times(1)).getAllTrainingTypes();
    }
}
