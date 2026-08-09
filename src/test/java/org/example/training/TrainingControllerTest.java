package org.example.training;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.TestUtils;
import org.example.config.AuthInterceptor;
import org.example.config.InterceptorConfigurationProperties;
import org.example.training.controller.TrainingController;
import org.example.training.controller.request.CreateTrainingRequest;
import org.example.training.service.TrainingService;
import org.example.trainingType.dto.TrainingType;
import org.example.trainingType.dto.TrainingTypes;
import org.example.trainingType.service.TrainingTypeService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TrainingController.class)
class TrainingControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        InterceptorConfigurationProperties interceptorConfigurationProperties() {
            return new InterceptorConfigurationProperties("/**");
        }
    }

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private TrainingService trainingService;

    @MockitoBean
    private TrainingTypeService trainingTypeService;

    @MockitoBean
    private AuthInterceptor authInterceptor;

    private static final UserCredentials CREDENTIALS = TestUtils.getTraineeCredentials();

    @BeforeEach
    void setUp() {
        when(authInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    void addTraining_Return200AndTrainingSummary_RequestIsValid() throws Exception {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TestUtils.TRAINER_USERNAME, TestUtils.TRAINEE_USERNAME, "Morning Cardio",
                LocalDate.now().plusDays(7), 45
        );
        when(trainingService.create(request, CREDENTIALS)).thenReturn(TestUtils.getTrainingSummary());

        mockMvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainingName").value("Morning Cardio"))
                .andExpect(jsonPath("$.trainingDate").value("2026-05-15"))
                .andExpect(jsonPath("$.durationMinutes").value(60));
        verify(trainingService, times(1)).create(request, CREDENTIALS);
    }

    @Test
    void addTraining_Return400AndProblemDetail_TrainerUsernameIsBlank() throws Exception {
        CreateTrainingRequest request = new CreateTrainingRequest(
                "  ", TestUtils.TRAINEE_USERNAME, "Cardio", LocalDate.now().plusDays(2), 45
        );

        mockMvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void addTraining_Return400AndProblemDetail_DurationBelowMinimum() throws Exception {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TestUtils.TRAINER_USERNAME, TestUtils.TRAINEE_USERNAME,
                "Cardio", LocalDate.now().plusDays(2), 10
        );

        mockMvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void addTraining_Return400AndProblemDetail_DurationAboveMaximum() throws Exception {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TestUtils.TRAINER_USERNAME, TestUtils.TRAINEE_USERNAME,
                "Cardio", LocalDate.now().plusDays(2), 400
        );

        mockMvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void addTraining_Return400AndProblemDetail_TrainingDateIsToday() throws Exception {
        CreateTrainingRequest request = new CreateTrainingRequest(
                TestUtils.TRAINER_USERNAME, TestUtils.TRAINEE_USERNAME, "Cardio", LocalDate.now(), 45
        );

        mockMvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void getTrainingTypes_Returns200AndTrainingTypes() throws Exception {
        TrainingTypes types = new TrainingTypes(List.of(TrainingType.YOGA, TrainingType.CARDIO));
        when(trainingTypeService.getAllTrainingTypes(CREDENTIALS)).thenReturn(types);

        mockMvc.perform(get("/api/v1/trainings/training-types")
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainingTypes").isArray())
                .andExpect(jsonPath("$.trainingTypes[0]").value("YOGA"))
                .andExpect(jsonPath("$.trainingTypes[1]").value("CARDIO"));
        verify(trainingTypeService, times(1)).getAllTrainingTypes(CREDENTIALS);
    }
}
