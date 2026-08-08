package org.example.trainee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.config.AuthInterceptor;
import org.example.config.InterceptorConfigurationProperties;
import org.example.exception.EntityNotFoundException;
import org.example.trainee.controller.TraineeController;
import org.example.trainee.controller.request.CreateTraineeRequest;
import org.example.trainee.controller.request.GetTraineeTrainingsRequest;
import org.example.trainee.controller.request.UpdateTraineeRequest;
import org.example.trainee.controller.request.UpdateTraineeTrainersRequest;
import org.example.trainee.service.TraineeService;
import org.example.trainer.controller.response.Trainers;
import org.example.training.controller.response.Trainings;
import org.example.training.service.TrainingService;
import org.example.trainingType.dto.TrainingType;
import org.example.user.controller.dto.FullName;
import org.example.user.controller.dto.UserCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.example.TestUtils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TraineeController.class)
class TraineeControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        InterceptorConfigurationProperties interceptorConfigurationProperties() {
            return new InterceptorConfigurationProperties("/**", "");
        }
    }

    private static final UserCredentials CREDENTIALS = getTraineeCredentials();

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private TraineeService traineeService;

    @MockitoBean
    private TrainingService trainingService;

    @MockitoBean
    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() {
        when(authInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    void registerTrainee_Return201AndTraineeSummary_RequestIsValid() throws Exception {
        CreateTraineeRequest request = new CreateTraineeRequest(
                new FullName("John", "Doe"), LocalDate.of(2000, 1, 1), "Home"
        );
        when(traineeService.create(request)).thenReturn(getTraineeSummary());

        mockMvc.perform(post("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TRAINEE_ID))
                .andExpect(jsonPath("$.profile.username").value(TRAINEE_USERNAME))
                .andExpect(jsonPath("$.dateOfBirth").value("2007-03-25"))
                .andExpect(jsonPath("$.address").value("Home 21 Street"));
        verify(traineeService, times(1)).create(request);
    }

    @Test
    void registerTrainee_Return400AndProblemDetail_FirstNameIsBlank() throws Exception {
        CreateTraineeRequest request = new CreateTraineeRequest(
                new FullName("  ", "Doe"), LocalDate.of(2000, 1, 1), "Home"
        );

        mockMvc.perform(post("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void registerTrainee_Return400AndProblemDetail_TraineeIsUnderage() throws Exception {
        CreateTraineeRequest request = new CreateTraineeRequest(
                new FullName("John", "Doe"), LocalDate.now().minusYears(10), "Home"
        );

        mockMvc.perform(post("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void getTrainee_Return200AndTraineeSummary_IdIsValid() throws Exception {
        when(traineeService.getById(TRAINEE_ID, CREDENTIALS)).thenReturn(getTraineeSummary());

        mockMvc.perform(get("/api/v1/trainees/{id}", TRAINEE_ID)
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TRAINEE_ID))
                .andExpect(jsonPath("$.profile.username").value(TRAINEE_USERNAME));
        verify(traineeService, times(1)).getById(TRAINEE_ID, CREDENTIALS);
    }

//    @Test
//    void getTrainee_Return401AndProblemDetail_UserUnauthorized() throws Exception {
//        UserCredentials invalidCredentials = new UserCredentials(TRAINEE_USERNAME, "InvalidPassword");
//        when(traineeService.getById(TRAINEE_ID, invalidCredentials))
//                .thenThrow(new AuthenticationFailedException("Authentication failed"));
//
//        mockMvc.perform(get("/api/v1/trainees/{id}", TRAINEE_ID)
//                        .requestAttr("userCredentials", invalidCredentials))
//                .andExpect(status().isUnauthorized())
//                .andExpect(jsonPath("$.title").value("Authentication Failure"))
//                .andExpect(jsonPath("$.detail").value("Authentication failed"));
//        verify(traineeService, times(1)).getById(TRAINEE_ID, invalidCredentials);
//    }

    @Test
    void getTrainee_Return404AndProblemDetail_TraineeNotFound() throws Exception {
        when(traineeService.getById(99L, CREDENTIALS))
                .thenThrow(new EntityNotFoundException("Trainee not found"));

        mockMvc.perform(get("/api/v1/trainees/{id}", 99L)
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Entity Not Found"))
                .andExpect(jsonPath("$.detail").value("Trainee not found"));
        verify(traineeService, times(1)).getById(99L, CREDENTIALS);
    }

    @Test
    void updateTrainee_Return200AndTraineeSummary_RequestIsValid() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                new FullName("John", "Doe"), LocalDate.of(2000, 1, 1), "Home");
        when(traineeService.update(TRAINEE_ID, request, CREDENTIALS)).thenReturn(getTraineeSummary());

        mockMvc.perform(put("/api/v1/trainees/{id}", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.username").value(TRAINEE_USERNAME));
        verify(traineeService, times(1)).update(TRAINEE_ID, request, CREDENTIALS);
    }

    @Test
    void updateTrainee_Return400AndProblemDetail_FullNameIsNull() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                null, LocalDate.of(2000, 1, 1), "Home");

        mockMvc.perform(put("/api/v1/trainees/{id}", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void deleteTrainee_Return200_UsernameIsValid() throws Exception {
        mockMvc.perform(delete("/api/v1/trainees")
                        .param("username", TRAINEE_USERNAME)
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isOk());
        verify(traineeService, times(1)).deleteByUsername(TRAINEE_USERNAME, CREDENTIALS);
    }

    @Test
    void updateTrainersList_Return200AndTrainers_RequestIsValid() throws Exception {
        Trainers trainers = new Trainers(List.of(getTrainerSummary()));
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(List.of(TRAINER_USERNAME));
        when(traineeService.updateTrainersList(TRAINEE_ID, request, CREDENTIALS)).thenReturn(trainers);

        mockMvc.perform(put("/api/v1/trainees/{id}/trainers-update", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainers").isArray())
                .andExpect(jsonPath("$.trainers[0].profile.username").value(TRAINER_USERNAME));
        verify(traineeService, times(1)).updateTrainersList(TRAINEE_ID, request, CREDENTIALS);
    }

    @Test
    void updateTrainersList_Return400AndProblemDetail_ListIsEmpty() throws Exception {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(List.of());

        mockMvc.perform(put("/api/v1/trainees/{id}/trainers-update", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void changeTraineeActivity_Return200_RequestIsValid() throws Exception {
        mockMvc.perform(patch("/api/v1/trainees/{id}/profile/active-status/change", TRAINEE_ID)
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isOk());
        verify(traineeService, times(1)).changeActivity(TRAINEE_ID, CREDENTIALS);
    }

    @Test
    void getTraineeTrainings_Return200AndTrainings_RequestIsValid() throws Exception {
        GetTraineeTrainingsRequest request = new GetTraineeTrainingsRequest(
                TRAINEE_USERNAME, LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), TRAINER_USERNAME, TrainingType.YOGA
        );
        Trainings trainings = new Trainings(List.of(getTrainingSummary()));
        when(trainingService.getTraineeTrainingList(request, CREDENTIALS)).thenReturn(trainings);

        mockMvc.perform(post("/api/v1/trainees/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainings").isArray())
                .andExpect(jsonPath("$.trainings[0].trainingName").value("Morning Cardio"));
        verify(trainingService, times(1)).getTraineeTrainingList(request, CREDENTIALS);
    }

    @Test
    void getTraineeTrainings_Return400_UsernameIsBlank() throws Exception {
        GetTraineeTrainingsRequest request = new GetTraineeTrainingsRequest(
                "  ", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), TRAINER_USERNAME, TrainingType.YOGA
        );
        mockMvc.perform(post("/api/v1/trainees/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }

    @Test
    void getTraineeTrainings_Return400_FromDateAfterToDate() throws Exception {
        GetTraineeTrainingsRequest request = new GetTraineeTrainingsRequest(
                TRAINEE_USERNAME, LocalDate.of(2026, 12, 31),
                LocalDate.of(2026, 1, 31), TRAINER_USERNAME, TrainingType.YOGA
        );
        mockMvc.perform(post("/api/v1/trainees/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }
}
