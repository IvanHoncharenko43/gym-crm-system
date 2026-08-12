package org.example.trainee;

import org.example.config.AuthInterceptor;
import org.example.config.InterceptorConfigurationProperties;
import org.example.exception.EntityNotFoundException;
import org.example.trainee.controller.TraineeController;
import org.example.trainee.controller.request.CreateTraineeRequest;
import org.example.trainee.controller.request.GetTraineeTrainingsRequest;
import org.example.trainee.controller.request.UpdateTraineeRequest;
import org.example.trainee.controller.request.UpdateTraineeTrainersRequest;
import org.example.trainee.controller.response.TraineeSummary;
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
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.List;

import static org.example.TestUtils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TraineeController.class)
class TraineeControllerIT {

    @TestConfiguration
    static class TestConfig {
        @Bean
        InterceptorConfigurationProperties interceptorConfigurationProperties() {
            return new InterceptorConfigurationProperties("/**");
        }
    }

    private static final UserCredentials CREDENTIALS = getTraineeCredentials();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

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
    void registerTrainee_Return201AndTraineeSummary_AllParametersProvidedAndValid() throws Exception {
        CreateTraineeRequest request = new CreateTraineeRequest(
                new FullName("John", "Doe"), LocalDate.of(2007, 3, 5), "Home"
        );
        TraineeSummary expectedResponse = getTraineeSummary();
        when(traineeService.create(request)).thenReturn(expectedResponse);

        mockMvc.perform(post("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(expectedResponse.id()))
                .andExpect(jsonPath("$.profile.username").value(expectedResponse.profile().username()))
                .andExpect(jsonPath("$.dateOfBirth").value(expectedResponse.dateOfBirth().toString()))
                .andExpect(jsonPath("$.address").value(expectedResponse.address()));
        verify(traineeService, times(1)).create(request);
    }

    @Test
    void registerTrainee_Return201AndTraineeSummary_OnlyRequiredParametersProvidedAndValid() throws Exception {
        CreateTraineeRequest request = new CreateTraineeRequest(
                new FullName("John", "Doe"), null, null
        );
        TraineeSummary expectedResponse = getTraineeSummaryWithNoOptional();
        when(traineeService.create(request)).thenReturn(expectedResponse);

        mockMvc.perform(post("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(expectedResponse.id()))
                .andExpect(jsonPath("$.profile.username").value(expectedResponse.profile().username()));
        verify(traineeService, times(1)).create(request);
    }

    @Test
    void registerTrainee_Return400AndProblemDetail_FullNameIsNull() throws Exception {
        CreateTraineeRequest request = new CreateTraineeRequest(
                null, LocalDate.of(2000, 1, 1), "Home"
        );

        mockMvc.perform(post("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void registerTrainee_Return400AndProblemDetail_FirstNameIsBlank() throws Exception {
        CreateTraineeRequest request = new CreateTraineeRequest(
                new FullName("  ", "Doe"), LocalDate.of(2000, 1, 1), "Home"
        );

        mockMvc.perform(post("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void registerTrainee_Return400AndProblemDetail_LastNameIsBlank() throws Exception {
        CreateTraineeRequest request = new CreateTraineeRequest(
                new FullName("John", ""), LocalDate.of(2000, 1, 1), "Home"
        );

        mockMvc.perform(post("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
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
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void registerTrainee_Return400AndProblemDetail_AddressIsTooLong() throws Exception {
        String address = "A".repeat(201);
        CreateTraineeRequest request = new CreateTraineeRequest(
                new FullName("John", "Doe"), LocalDate.of(2000, 1, 1), address
        );

        mockMvc.perform(post("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void getTrainee_Return200AndTraineeSummary_IdIsValid() throws Exception {
        TraineeSummary expectedResponse = getTraineeSummary();
        when(traineeService.getById(TRAINEE_ID, CREDENTIALS)).thenReturn(expectedResponse);

        mockMvc.perform(get("/api/v1/trainees/{id}", TRAINEE_ID)
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResponse.id()))
                .andExpect(jsonPath("$.profile.username").value(expectedResponse.profile().username()))
                .andExpect(jsonPath("$.dateOfBirth").value(expectedResponse.dateOfBirth().toString()))
                .andExpect(jsonPath("$.address").value(expectedResponse.address()));
        verify(traineeService, times(1)).getById(TRAINEE_ID, CREDENTIALS);
    }

    @Test
    void getTrainee_Return404AndProblemDetail_TraineeNotFound() throws Exception {
        Long id = 99L;
        when(traineeService.getById(id, CREDENTIALS))
                .thenThrow(new EntityNotFoundException("Trainee not found"));

        mockMvc.perform(get("/api/v1/trainees/{id}", id)
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Entity Not Found"))
                .andExpect(jsonPath("$.detail").value("Trainee not found"));
        verify(traineeService, times(1)).getById(id, CREDENTIALS);
    }

    @Test
    void updateTrainee_Return200AndTraineeSummary_AllParametersProvidedAndValid() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                new FullName("John", "Doe"), LocalDate.of(2000, 1, 1), "Home");
        TraineeSummary expectedResponse = getTraineeSummary();
        when(traineeService.update(TRAINEE_ID, request, CREDENTIALS)).thenReturn(expectedResponse);

        mockMvc.perform(put("/api/v1/trainees/{id}", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResponse.id()))
                .andExpect(jsonPath("$.profile.username").value(expectedResponse.profile().username()))
                .andExpect(jsonPath("$.dateOfBirth").value(expectedResponse.dateOfBirth().toString()))
                .andExpect(jsonPath("$.address").value(expectedResponse.address()));
        verify(traineeService, times(1)).update(TRAINEE_ID, request, CREDENTIALS);
    }

    @Test
    void updateTrainee_Return200AndTraineeSummary_OnlyRequiredParametersProvidedAndValid() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                new FullName("John", "Doe"), null, null);
        TraineeSummary expectedResponse = getTraineeSummaryWithNoOptional();
        when(traineeService.update(TRAINEE_ID, request, CREDENTIALS))
                .thenReturn(expectedResponse);

        mockMvc.perform(put("/api/v1/trainees/{id}", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResponse.id()))
                .andExpect(jsonPath("$.profile.username").value(expectedResponse.profile().username()));
        verify(traineeService, times(1)).update(TRAINEE_ID, request, CREDENTIALS);
    }

    @Test
    void updateTrainee_Return400AndProblemDetail_FullNameIsNull() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                null, LocalDate.of(2000, 1, 1), "Home");

        mockMvc.perform(put("/api/v1/trainees/{id}", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void updateTrainee_Return400AndProblemDetail_FirstNameIsBlank() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                new FullName("  ", "Doe"), LocalDate.of(2000, 1, 1), "Home"
        );

        mockMvc.perform(put("/api/v1/trainees/{id}", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void updateTrainee_Return400AndProblemDetail_LastNameIsBlank() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                new FullName("John", ""), LocalDate.of(2000, 1, 1), "Home"
        );

        mockMvc.perform(put("/api/v1/trainees/{id}", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void updateTrainee_Return400AndProblemDetail_TraineeIsUnderage() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                new FullName("John", "Doe"), LocalDate.now().minusYears(10), "Home"
        );

        mockMvc.perform(put("/api/v1/trainees/{id}", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void updateTrainee_Return400AndProblemDetail_AddressIsTooLong() throws Exception {
        String address = "A".repeat(201);
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                new FullName("John", "Doe"), LocalDate.of(2000, 1, 1), address
        );

        mockMvc.perform(put("/api/v1/trainees/{id}", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void updateTrainee_Return404AndProblemDetail_TraineeNotFound() throws Exception {
        Long id = 99L;
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                new FullName("John", "Doe"), LocalDate.of(2000, 1, 1), "Home");
        when(traineeService.update(id, request, CREDENTIALS))
                .thenThrow(new EntityNotFoundException("Trainee not found"));

        mockMvc.perform(put("/api/v1/trainees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Entity Not Found"));
        verify(traineeService, times(1)).update(id, request, CREDENTIALS);
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
                        .content(jsonMapper.writeValueAsString(request))
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
                        .content(jsonMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void updateTrainersList_Return400AndProblemDetail_UsernameInTheListIsBlank() throws Exception {
        List<String> usernames = List.of("John.Doe", " ", "John.Doe1");
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(usernames);

        mockMvc.perform(put("/api/v1/trainees/{id}/trainers-update", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void updateTrainersList_Return404AndProblemDetail_TraineeNotFound() throws Exception {
        Long id = 99L;
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(List.of(TRAINER_USERNAME));
        when(traineeService.updateTrainersList(id, request, CREDENTIALS))
                .thenThrow(new EntityNotFoundException("Trainee not found"));

        mockMvc.perform(put("/api/v1/trainees/{id}/trainers-update", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Entity Not Found"));
        verify(traineeService, times(1)).updateTrainersList(id, request, CREDENTIALS);
    }

    @Test
    void changeTraineeActivity_Return200_RequestIsValid() throws Exception {
        mockMvc.perform(patch("/api/v1/trainees/{id}/profile/active-status/change", TRAINEE_ID)
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isOk());
        verify(traineeService, times(1)).changeActivity(TRAINEE_ID, CREDENTIALS);
    }

    @Test
    void changeTraineeActivity_Return404AndProblemDetail_TraineeNotFound() throws Exception {
        Long id = 99L;
        doThrow(new EntityNotFoundException("Trainee not found"))
                .when(traineeService).changeActivity(id, CREDENTIALS);

        mockMvc.perform(patch("/api/v1/trainees/{id}/profile/active-status/change", id)
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Entity Not Found"));
        verify(traineeService, times(1)).changeActivity(id, CREDENTIALS);
    }

    @Test
    void getTraineeTrainings_Return200AndTrainings_AllParametersProvidedAndValid() throws Exception {
        GetTraineeTrainingsRequest request = new GetTraineeTrainingsRequest(
                TRAINEE_USERNAME, LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), TRAINER_USERNAME, TrainingType.YOGA
        );
        Trainings trainings = new Trainings(List.of(getTrainingSummary()));
        when(trainingService.getTraineeTrainingList(request, CREDENTIALS)).thenReturn(trainings);

        mockMvc.perform(post("/api/v1/trainees/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainings").isArray())
                .andExpect(jsonPath("$.trainings[0].trainingName").value("Morning Cardio"));
        verify(trainingService, times(1)).getTraineeTrainingList(request, CREDENTIALS);
    }

    @Test
    void getTraineeTrainings_Return200AndTrainings_OnlyRequiredParametersProvidedAndValid() throws Exception {
        GetTraineeTrainingsRequest request = new GetTraineeTrainingsRequest(
                TRAINEE_USERNAME, null, null, null, null
        );
        Trainings trainings = new Trainings(List.of(getTrainingSummary()));
        when(trainingService.getTraineeTrainingList(request, CREDENTIALS)).thenReturn(trainings);

        mockMvc.perform(post("/api/v1/trainees/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainings").isArray())
                .andExpect(jsonPath("$.trainings[0].trainingName").value("Morning Cardio"));
        verify(trainingService, times(1)).getTraineeTrainingList(request, CREDENTIALS);
    }

    @Test
    void getTraineeTrainings_Return200AndTrainings_ToDateAbsent() throws Exception {
        GetTraineeTrainingsRequest request = new GetTraineeTrainingsRequest(
                TRAINEE_USERNAME, LocalDate.of(2026, 1, 1), null, null, null
        );
        Trainings trainings = new Trainings(List.of(getTrainingSummary()));
        when(trainingService.getTraineeTrainingList(request, CREDENTIALS)).thenReturn(trainings);

        mockMvc.perform(post("/api/v1/trainees/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainings").isArray())
                .andExpect(jsonPath("$.trainings[0].trainingName").value("Morning Cardio"));
        verify(trainingService, times(1)).getTraineeTrainingList(request, CREDENTIALS);
    }

    @Test
    void getTraineeTrainings_Return200AndTrainings_FromDateAbsent() throws Exception {
        GetTraineeTrainingsRequest request = new GetTraineeTrainingsRequest(
                TRAINEE_USERNAME, null, LocalDate.of(2026, 12, 1), null, null
        );
        Trainings trainings = new Trainings(List.of(getTrainingSummary()));
        when(trainingService.getTraineeTrainingList(request, CREDENTIALS)).thenReturn(trainings);

        mockMvc.perform(post("/api/v1/trainees/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request))
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
                        .content(jsonMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());;
    }

    @Test
    void getTraineeTrainings_Return400_FromDateAfterToDate() throws Exception {
        GetTraineeTrainingsRequest request = new GetTraineeTrainingsRequest(
                TRAINEE_USERNAME, LocalDate.of(2026, 12, 31),
                LocalDate.of(2026, 1, 31), TRAINER_USERNAME, TrainingType.YOGA
        );
        mockMvc.perform(post("/api/v1/trainees/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());;
    }

    @Test
    void getTraineeTrainings_Return400_TrainerNameIsTooLong() throws Exception {
        String trainerName = "A".repeat(51);
        GetTraineeTrainingsRequest request = new GetTraineeTrainingsRequest(
                TRAINEE_USERNAME, LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), trainerName, TrainingType.YOGA
        );
        mockMvc.perform(post("/api/v1/trainees/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());;
    }
}
