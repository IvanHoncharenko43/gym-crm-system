package org.example.crm.training;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.example.crm.TestUtils;
import org.example.crm.config.SecurityConfig;
import org.example.crm.exception.AccessForbiddenException;
import org.example.crm.exception.EntityNotFoundException;
import org.example.crm.exception.InvalidRequestDataException;
import org.example.crm.security.service.JwtService;
import org.example.crm.security.service.OwnershipVerifier;
import org.example.crm.security.service.TokenBlackListService;
import org.example.crm.training.controller.TrainingController;
import org.example.crm.training.controller.request.CreateTrainingRequest;
import org.example.crm.training.controller.response.TrainingSummary;
import org.example.crm.training.service.TrainingService;
import org.example.crm.trainingType.dto.TrainingType;
import org.example.crm.trainingType.dto.TrainingTypes;
import org.example.crm.trainingType.service.TrainingTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.example.crm.TestUtils.*;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.Mockito.*;

@WebMvcTest(controllers = TrainingController.class)
@Import(SecurityConfig.class)
class TrainingControllerIT {

    private static final UserDetails USER_DETAILS = getTrainerUserDetails();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainingService trainingService;

    @MockitoBean
    private TrainingTypeService trainingTypeService;

    @MockitoBean
    private OwnershipVerifier ownershipVerifier;

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
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD, roles = {"TRAINER"})
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
        verify(ownershipVerifier, times(1)).verifyOwnership(TRAINER_USERNAME, USER_DETAILS);
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
    @WithAnonymousUser
    void addTraining_Return401AndProblemDetail_UserIsUnauthenticated() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TRAINER_USERNAME, TRAINEE_USERNAME, "Morning Cardio",
                LocalDate.now().plusDays(7), 45
        );

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post("/api/v1/trainings")
                .then()
                .status(HttpStatus.UNAUTHORIZED)
                .body("title", equalTo("Authentication Failure"))
                .body("detail", equalTo("Authentication token is missing"));
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD, roles = {"TRAINER"})
    void cancelTraining_Return200_RequestIsValid() {
        given()
                .when()
                .delete("/api/v1/trainings/{id}", TestUtils.TRAINING_ID)
                .then()
                .status(HttpStatus.OK);
        verify(ownershipVerifier, times(1)).verifyOwnership(TestUtils.TRAINING_ID, USER_DETAILS, OwnershipVerifier.ResourceType.TRAINING);
        verify(trainingService, times(1)).cancel(TestUtils.TRAINING_ID);
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD, roles = {"TRAINER"})
    void cancelTraining_Return400AndProblemDetail_TrainingDateHasAlreadyPassed() {
        doThrow(new InvalidRequestDataException("Cannot cancel training with ID 1 as training date has already passed"))
                .when(trainingService).cancel(TestUtils.TRAINING_ID);

        given()
                .when()
                .delete("/api/v1/trainings/{id}", TestUtils.TRAINING_ID)
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("title", equalTo("Invalid Request Data"))
                .body("detail", equalTo("Cannot cancel training with ID 1 as training date has already passed"));

        verify(ownershipVerifier, times(1)).verifyOwnership(TestUtils.TRAINING_ID, USER_DETAILS, OwnershipVerifier.ResourceType.TRAINING);
        verify(trainingService, times(1)).cancel(TestUtils.TRAINING_ID);
    }

    @Test
    @WithAnonymousUser
    void cancelTraining_Return401AndProblemDetail_UserIsUnauthenticated() {
        given()
                .when()
                .delete("/api/v1/trainings/{id}", TestUtils.TRAINING_ID)
                .then()
                .status(HttpStatus.UNAUTHORIZED)
                .body("title", equalTo("Authentication Failure"))
                .body("detail", equalTo("Authentication token is missing"));
        verify(trainingService, never()).cancel(any());
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD, roles = {"TRAINEE"})
    void cancelTraining_Return403AndProblemDetail_UserRoleIsInvalid() {
        given()
                .when()
                .delete("/api/v1/trainings/{id}", TestUtils.TRAINING_ID)
                .then()
                .status(HttpStatus.FORBIDDEN)
                .body("title", equalTo("Authorization Failure"))
                .body("detail", equalTo("Not enough rights to access the resource"));
        verify(trainingService, never()).cancel(any());
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD, roles = {"TRAINER"})
    void cancelTraining_Return403AndProblemDetail_UserIsNotOwner() {
        doThrow(new AccessForbiddenException("Authorization failed"))
                .when(ownershipVerifier).verifyOwnership(TestUtils.TRAINING_ID, USER_DETAILS, OwnershipVerifier.ResourceType.TRAINING);

        given()
                .when()
                .delete("/api/v1/trainings/{id}", TestUtils.TRAINING_ID)
                .then()
                .status(HttpStatus.FORBIDDEN)
                .body("title", equalTo("Authorization Failure"))
                .body("detail", equalTo("Authorization failed"));
        verify(trainingService, never()).cancel(any());
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD, roles = {"TRAINER"})
    void cancelTraining_Return404AndProblemDetail_TrainingNotFound() {
        doThrow(new EntityNotFoundException("Training with ID 1 not found"))
                .when(trainingService).cancel(TestUtils.TRAINING_ID);

        given()
                .when()
                .delete("/api/v1/trainings/{id}", TestUtils.TRAINING_ID)
                .then()
                .status(HttpStatus.NOT_FOUND)
                .body("title", equalTo("Entity Not Found"))
                .body("detail", equalTo("Training with ID 1 not found"));
        verify(ownershipVerifier, times(1)).verifyOwnership(TestUtils.TRAINING_ID, USER_DETAILS, OwnershipVerifier.ResourceType.TRAINING);
        verify(trainingService, times(1)).cancel(TestUtils.TRAINING_ID);
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

    @Test
    @WithAnonymousUser
    void getTrainingTypes_Return401AndProblemDetail_UserIsUnauthenticated() {
        given()
                .when()
                .get("/api/v1/trainings/training-types")
                .then()
                .status(HttpStatus.UNAUTHORIZED)
                .body("title", equalTo("Authentication Failure"))
                .body("detail", equalTo("Authentication token is missing"));
    }
}
