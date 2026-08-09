package org.example.trainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.config.AuthInterceptor;
import org.example.config.InterceptorConfigurationProperties;
import org.example.exception.EntityNotFoundException;
import org.example.trainer.controller.TrainerController;
import org.example.trainer.controller.request.CreateTrainerRequest;
import org.example.trainer.controller.request.GetTrainerTrainingsRequest;
import org.example.trainer.controller.request.UpdateTrainerRequest;
import org.example.trainer.controller.response.Trainers;
import org.example.trainer.service.TrainerService;
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

@WebMvcTest(controllers = TrainerController.class)
class TrainerControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        InterceptorConfigurationProperties interceptorConfigurationProperties() {
            return new InterceptorConfigurationProperties("/**");
        }
    }

    private static final UserCredentials CREDENTIALS = getTrainerCredentials();

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private TrainerService trainerService;

    @MockitoBean
    private TrainingService trainingService;

    @MockitoBean
    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() {
        when(authInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    void registerTrainer_Return201AndTrainerSummary_RequestIsValid() throws Exception {
        CreateTrainerRequest request = new CreateTrainerRequest(
                new FullName("John", "Doe"), TrainingType.YOGA
        );
        when(trainerService.create(request)).thenReturn(getTrainerSummary());

        mockMvc.perform(post("/api/v1/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TRAINER_ID))
                .andExpect(jsonPath("$.profile.username").value(TRAINER_USERNAME))
                .andExpect(jsonPath("$.specialization").value("YOGA"));
        verify(trainerService, times(1)).create(request);
    }

    @Test
    void registerTrainer_Return400AndProblemDetail_FirstNameIsBlank() throws Exception {
        CreateTrainerRequest request = new CreateTrainerRequest(
                new FullName("  ", "Doe"), TrainingType.YOGA
        );

        mockMvc.perform(post("/api/v1/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void registerTrainer_Return400AndProblemDetail_SpecializationIsNull() throws Exception {
        CreateTrainerRequest request = new CreateTrainerRequest(
                new FullName("John", "Doe"), null
        );

        mockMvc.perform(post("/api/v1/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void getTrainer_Return200AndTrainerSummary_IdIsValid() throws Exception {
        when(trainerService.getById(TRAINER_ID, CREDENTIALS)).thenReturn(getTrainerSummary());

        mockMvc.perform(get("/api/v1/trainers/{id}", TRAINER_ID)
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TRAINER_ID))
                .andExpect(jsonPath("$.profile.username").value(TRAINER_USERNAME));
        verify(trainerService, times(1)).getById(TRAINER_ID, CREDENTIALS);
    }

    @Test
    void getTrainer_Return404AndProblemDetail_TrainerNotFound() throws Exception {
        when(trainerService.getById(99L, CREDENTIALS))
                .thenThrow(new EntityNotFoundException("Trainer not found"));

        mockMvc.perform(get("/api/v1/trainers/{id}", 99L)
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Entity Not Found"))
                .andExpect(jsonPath("$.detail").value("Trainer not found"));
        verify(trainerService, times(1)).getById(99L, CREDENTIALS);
    }

    @Test
    void updateTrainer_Return200AndTrainerSummary_RequestIsValid() throws Exception {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                new FullName("John", "Doe"), TrainingType.YOGA);
        when(trainerService.update(TRAINER_ID, request, CREDENTIALS)).thenReturn(getTrainerSummary());

        mockMvc.perform(put("/api/v1/trainers/{id}", TRAINER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.username").value(TRAINER_USERNAME));
        verify(trainerService, times(1)).update(TRAINER_ID, request, CREDENTIALS);
    }

    @Test
    void updateTrainer_Return400AndProblemDetail_FullNameIsNull() throws Exception {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                null, TrainingType.YOGA);

        mockMvc.perform(put("/api/v1/trainers/{id}", TRAINER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void getNotAssignedTrainers_Return200AndTrainers_RequestIsValid() throws Exception {
        Trainers trainers = new Trainers(List.of(getTrainerSummary()));
        when(trainerService.getUnassignedTrainersByTraineeList(TRAINEE_USERNAME, CREDENTIALS))
                .thenReturn(trainers);

        mockMvc.perform(get("/api/v1/trainers/not-assigned")
                        .param("trainee-username", TRAINEE_USERNAME)
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainers").isArray())
                .andExpect(jsonPath("$.trainers[0].profile.username").value(TRAINER_USERNAME));
        verify(trainerService, times(1)).getUnassignedTrainersByTraineeList(TRAINEE_USERNAME, CREDENTIALS);
    }

    @Test
    void getNotAssignedTrainers_Return400AndProblemDetail_TraineeUsernameIsBlank() throws Exception {
        mockMvc.perform(get("/api/v1/trainers/not-assigned")
                        .param("trainee-username", "  ")
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.violations").isNotEmpty());
    }

    @Test
    void changeTrainerActivity_Return200_RequestIsValid() throws Exception {
        mockMvc.perform(patch("/api/v1/trainers/{id}/activity-change", TRAINER_ID)
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isOk());
        verify(trainerService, times(1)).changeActivity(TRAINER_ID, CREDENTIALS);
    }

    @Test
    void getTrainerTrainings_Return200AndTrainings_RequestIsValid() throws Exception {
        GetTrainerTrainingsRequest request = new GetTrainerTrainingsRequest(
                TRAINER_USERNAME, LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), TRAINEE_USERNAME
        );
        Trainings trainings = new Trainings(List.of(getTrainingSummary()));
        when(trainingService.getTrainerTrainingList(request, CREDENTIALS)).thenReturn(trainings);

        mockMvc.perform(post("/api/v1/trainers/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainings").isArray())
                .andExpect(jsonPath("$.trainings[0].trainingName").value("Morning Cardio"));
        verify(trainingService, times(1)).getTrainerTrainingList(request, CREDENTIALS);
    }

    @Test
    void getTrainerTrainings_Return400_UsernameIsBlank() throws Exception {
        GetTrainerTrainingsRequest request = new GetTrainerTrainingsRequest(
                "  ", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), TRAINEE_USERNAME
        );
        mockMvc.perform(post("/api/v1/trainers/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }

    @Test
    void getTrainerTrainings_Return400_FromDateAfterToDate() throws Exception {
        GetTrainerTrainingsRequest request = new GetTrainerTrainingsRequest(
                TRAINER_USERNAME, LocalDate.of(2026, 12, 31),
                LocalDate.of(2026, 1, 31), TRAINEE_USERNAME
        );
        mockMvc.perform(post("/api/v1/trainers/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }
}
