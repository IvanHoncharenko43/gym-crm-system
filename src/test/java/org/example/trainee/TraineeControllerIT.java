package org.example.trainee;

import org.example.config.SecurityConfig;
import org.example.exception.EntityNotFoundException;
import org.example.security.controller.dto.LoginDetails;
import org.example.security.service.JwtService;
import org.example.security.service.TokenBlackListService;
import org.example.trainee.controller.TraineeController;
import org.example.trainee.controller.request.CreateTraineeRequest;
import org.example.trainee.controller.request.GetTraineeTrainingsRequest;
import org.example.trainee.controller.request.UpdateTraineeRequest;
import org.example.trainee.controller.request.UpdateTraineeTrainersRequest;
import org.example.trainee.controller.response.TraineeRegistrationResponse;
import org.example.trainee.controller.response.TraineeSummary;
import org.example.trainee.service.TraineeService;
import org.example.trainer.controller.response.Trainers;
import org.example.training.controller.response.Trainings;
import org.example.training.service.TrainingService;
import org.example.trainingType.dto.TrainingType;
import org.example.user.controller.dto.FullName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.List;

import static org.example.TestUtils.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TraineeController.class)
@Import(SecurityConfig.class)
class TraineeControllerIT {

    private static final UserDetails USER_DETAILS = getTraineeUserDetails();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private TraineeService traineeService;

    @MockitoBean
    private TrainingService trainingService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private TokenBlackListService tokenBlackListService;

    @Test
    void registerTrainee_Return201AndTraineeSummary_AllParametersProvidedAndValid() throws Exception {
        CreateTraineeRequest request = new CreateTraineeRequest(
                new FullName("John", "Doe"), LocalDate.of(2007, 3, 5), "Home"
        );
        TraineeRegistrationResponse expectedResponse = new TraineeRegistrationResponse(getTraineeSummary(), new LoginDetails("token"));
        when(traineeService.create(request)).thenReturn(expectedResponse);

        mockMvc.perform(post("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.traineeSummary.id").value(expectedResponse.traineeSummary().id()))
                .andExpect(jsonPath("$.traineeSummary.profile.username").value(expectedResponse.traineeSummary().profile().username()))
                .andExpect(jsonPath("$.traineeSummary.dateOfBirth").value(expectedResponse.traineeSummary().dateOfBirth().toString()))
                .andExpect(jsonPath("$.traineeSummary.address").value(expectedResponse.traineeSummary().address()))
                .andExpect(jsonPath("$.loginDetails.token").value(expectedResponse.loginDetails().token()));
        verify(traineeService, times(1)).create(request);
    }

    @Test
    void registerTrainee_Return201AndTraineeSummary_OnlyRequiredParametersProvidedAndValid() throws Exception {
        CreateTraineeRequest request = new CreateTraineeRequest(
                new FullName("John", "Doe"), null, null
        );
        TraineeRegistrationResponse expectedResponse = new TraineeRegistrationResponse(getTraineeSummaryWithNoOptional(), new LoginDetails("token"));
        when(traineeService.create(request)).thenReturn(expectedResponse);

        mockMvc.perform(post("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.traineeSummary.id").value(expectedResponse.traineeSummary().id()))
                .andExpect(jsonPath("$.traineeSummary.profile.username").value(expectedResponse.traineeSummary().profile().username()))
                .andExpect(jsonPath("$.loginDetails.token").value(expectedResponse.loginDetails().token()));;
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
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD)
    void getTrainee_Return200AndTraineeSummary_IdIsValid() throws Exception {
        TraineeSummary expectedResponse = getTraineeSummary();
        when(traineeService.getById(TRAINEE_ID)).thenReturn(expectedResponse);

        mockMvc.perform(get("/api/v1/trainees/{id}", TRAINEE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResponse.id()))
                .andExpect(jsonPath("$.profile.username").value(expectedResponse.profile().username()))
                .andExpect(jsonPath("$.dateOfBirth").value(expectedResponse.dateOfBirth().toString()))
                .andExpect(jsonPath("$.address").value(expectedResponse.address()));
        verify(traineeService, times(1)).getById(TRAINEE_ID);
    }

    @Test
    @WithAnonymousUser
    void getTrainee_Return401AndProblemDetail_UserIsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/trainees/{id}", TRAINEE_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication Failure"))
                .andExpect(jsonPath("$.detail").value("Authentication failed during accessing the resource"));
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD)
    void getTrainee_Return404AndProblemDetail_TraineeNotFound() throws Exception {
        Long id = 99L;
        when(traineeService.getById(id))
                .thenThrow(new EntityNotFoundException("Trainee not found"));

        mockMvc.perform(get("/api/v1/trainees/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Entity Not Found"))
                .andExpect(jsonPath("$.detail").value("Trainee not found"));
        verify(traineeService, times(1)).getById(id);
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void updateTrainee_Return200AndTraineeSummary_AllParametersProvidedAndValid() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                new FullName("John", "Doe"), LocalDate.of(2000, 1, 1), "Home");
        TraineeSummary expectedResponse = getTraineeSummary();
        when(traineeService.update(TRAINEE_ID, request, USER_DETAILS)).thenReturn(expectedResponse);

        mockMvc.perform(put("/api/v1/trainees/{id}", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResponse.id()))
                .andExpect(jsonPath("$.profile.username").value(expectedResponse.profile().username()))
                .andExpect(jsonPath("$.dateOfBirth").value(expectedResponse.dateOfBirth().toString()))
                .andExpect(jsonPath("$.address").value(expectedResponse.address()));
        verify(traineeService, times(1)).update(TRAINEE_ID, request, USER_DETAILS);
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void updateTrainee_Return200AndTraineeSummary_OnlyRequiredParametersProvidedAndValid() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                new FullName("John", "Doe"), null, null);
        TraineeSummary expectedResponse = getTraineeSummaryWithNoOptional();
        when(traineeService.update(TRAINEE_ID, request, USER_DETAILS))
                .thenReturn(expectedResponse);

        mockMvc.perform(put("/api/v1/trainees/{id}", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResponse.id()))
                .andExpect(jsonPath("$.profile.username").value(expectedResponse.profile().username()));
        verify(traineeService, times(1)).update(TRAINEE_ID, request, USER_DETAILS);
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void updateTrainee_Return400AndProblemDetail_FullNameIsNull() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                null, LocalDate.of(2000, 1, 1), "Home");

        mockMvc.perform(put("/api/v1/trainees/{id}", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void updateTrainee_Return400AndProblemDetail_FirstNameIsBlank() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                new FullName("  ", "Doe"), LocalDate.of(2000, 1, 1), "Home"
        );

        mockMvc.perform(put("/api/v1/trainees/{id}", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void updateTrainee_Return400AndProblemDetail_LastNameIsBlank() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                new FullName("John", ""), LocalDate.of(2000, 1, 1), "Home"
        );

        mockMvc.perform(put("/api/v1/trainees/{id}", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void updateTrainee_Return400AndProblemDetail_TraineeIsUnderage() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                new FullName("John", "Doe"), LocalDate.now().minusYears(10), "Home"
        );

        mockMvc.perform(put("/api/v1/trainees/{id}", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void updateTrainee_Return400AndProblemDetail_AddressIsTooLong() throws Exception {
        String address = "A".repeat(201);
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                new FullName("John", "Doe"), LocalDate.of(2000, 1, 1), address
        );

        mockMvc.perform(put("/api/v1/trainees/{id}", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    @WithAnonymousUser
    void updateTrainee_Return401AndProblemDetail_UserIsUnauthenticated() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                new FullName("John", "Doe"), LocalDate.of(2000, 1, 1), "Home");

        mockMvc.perform(put("/api/v1/trainees/{id}", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication Failure"))
                .andExpect(jsonPath("$.detail").value("Authentication failed during accessing the resource"));
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD, roles = {"TRAINER"})
    void updateTrainee_Return403AndProblemDetail_UserRoleIsInvalid() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                new FullName("John", "Doe"), LocalDate.of(2000, 1, 1), "Home");

        mockMvc.perform(put("/api/v1/trainees/{id}", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Authorization Failure"))
                .andExpect(jsonPath("$.detail").value("Authorization failed during accessing the resource"));
        verify(traineeService, never()).update(anyLong(), any(), any());
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void updateTrainee_Return404AndProblemDetail_TraineeNotFound() throws Exception {
        Long id = 99L;
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                new FullName("John", "Doe"), LocalDate.of(2000, 1, 1), "Home");
        when(traineeService.update(id, request, USER_DETAILS))
                .thenThrow(new EntityNotFoundException("Trainee not found"));

        mockMvc.perform(put("/api/v1/trainees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Entity Not Found"));
        verify(traineeService, times(1)).update(id, request, USER_DETAILS);
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void deleteTrainee_Return200_UsernameIsValid() throws Exception {
        mockMvc.perform(delete("/api/v1/trainees")
                        .param("username", TRAINEE_USERNAME))
                .andExpect(status().isOk());
        verify(traineeService, times(1)).deleteByUsername(TRAINEE_USERNAME, USER_DETAILS);
    }

    @Test
    @WithAnonymousUser
    void deleteTrainee_Return401AndProblemDetail_UserIsUnauthenticated() throws Exception {
        mockMvc.perform(delete("/api/v1/trainees")
                        .param("username", TRAINEE_USERNAME))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication Failure"))
                .andExpect(jsonPath("$.detail").value("Authentication failed during accessing the resource"));
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD, roles = {"TRAINER"})
    void deleteTrainee_Return403AndProblemDetail_UserRoleIsInvalid() throws Exception {
        mockMvc.perform(delete("/api/v1/trainees")
                        .param("username", TRAINEE_USERNAME))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Authorization Failure"))
                .andExpect(jsonPath("$.detail").value("Authorization failed during accessing the resource"));
        verify(traineeService, never()).deleteByUsername(any(), any());
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void updateTrainersList_Return200AndTrainers_RequestIsValid() throws Exception {
        Trainers trainers = new Trainers(List.of(getTrainerSummary()));
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(List.of(TRAINER_USERNAME));
        when(traineeService.updateTrainersList(TRAINEE_ID, request, USER_DETAILS)).thenReturn(trainers);

        mockMvc.perform(put("/api/v1/trainees/{id}/trainers-update", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainers").isArray())
                .andExpect(jsonPath("$.trainers[0].profile.username").value(TRAINER_USERNAME));
        verify(traineeService, times(1)).updateTrainersList(TRAINEE_ID, request, USER_DETAILS);
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void updateTrainersList_Return400AndProblemDetail_ListIsEmpty() throws Exception {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(List.of());

        mockMvc.perform(put("/api/v1/trainees/{id}/trainers-update", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void updateTrainersList_Return400AndProblemDetail_UsernameInTheListIsBlank() throws Exception {
        List<String> usernames = List.of("John.Doe", " ", "John.Doe1");
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(usernames);

        mockMvc.perform(put("/api/v1/trainees/{id}/trainers-update", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    @WithAnonymousUser
    void updateTrainersList_Return401AndProblemDetail_UserIsUnauthenticated() throws Exception {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(List.of(TRAINER_USERNAME));

        mockMvc.perform(put("/api/v1/trainees/{id}/trainers-update", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication Failure"))
                .andExpect(jsonPath("$.detail").value("Authentication failed during accessing the resource"));
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD, roles = {"TRAINER"})
    void updateTrainersList_Return403AndProblemDetail_UserRoleIsInvalid() throws Exception {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(List.of(TRAINER_USERNAME));

        mockMvc.perform(put("/api/v1/trainees/{id}/trainers-update", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Authorization Failure"))
                .andExpect(jsonPath("$.detail").value("Authorization failed during accessing the resource"));
        verify(traineeService, never()).updateTrainersList(anyLong(), any(), any());
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void updateTrainersList_Return404AndProblemDetail_TraineeNotFound() throws Exception {
        Long id = 99L;
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(List.of(TRAINER_USERNAME));
        when(traineeService.updateTrainersList(id, request, USER_DETAILS))
                .thenThrow(new EntityNotFoundException("Trainee not found"));

        mockMvc.perform(put("/api/v1/trainees/{id}/trainers-update", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Entity Not Found"));
        verify(traineeService, times(1)).updateTrainersList(id, request, USER_DETAILS);
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void changeTraineeActivity_Return200_RequestIsValid() throws Exception {
        mockMvc.perform(patch("/api/v1/trainees/{id}/profile/active-status/change", TRAINEE_ID))
                .andExpect(status().isOk());
        verify(traineeService, times(1)).changeActivity(TRAINEE_ID, USER_DETAILS);
    }

    @Test
    @WithAnonymousUser
    void changeTraineeActivity_Return401AndProblemDetail_UserIsUnauthenticated() throws Exception {
        mockMvc.perform(patch("/api/v1/trainees/{id}/profile/active-status/change", TRAINER_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication Failure"))
                .andExpect(jsonPath("$.detail").value("Authentication failed during accessing the resource"));
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD, roles = {"TRAINER"})
    void changeTraineeActivity_Return403AndProblemDetail_UserRoleIsInvalid() throws Exception {
        mockMvc.perform(patch("/api/v1/trainees/{id}/profile/active-status/change", TRAINER_ID))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Authorization Failure"))
                .andExpect(jsonPath("$.detail").value("Authorization failed during accessing the resource"));
        verify(traineeService, never()).changeActivity(anyLong(), any());
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void changeTraineeActivity_Return404AndProblemDetail_TraineeNotFound() throws Exception {
        Long id = 99L;
        doThrow(new EntityNotFoundException("Trainee not found"))
                .when(traineeService).changeActivity(id, USER_DETAILS);

        mockMvc.perform(patch("/api/v1/trainees/{id}/profile/active-status/change", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Entity Not Found"));
        verify(traineeService, times(1)).changeActivity(id, USER_DETAILS);
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD)
    void getTraineeTrainings_Return200AndTrainings_AllParametersProvidedAndValid() throws Exception {
        GetTraineeTrainingsRequest request = new GetTraineeTrainingsRequest(
                TRAINEE_USERNAME, LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), TRAINER_USERNAME, TrainingType.YOGA
        );
        Trainings trainings = new Trainings(List.of(getTrainingSummary()));
        when(trainingService.getTraineeTrainingList(request)).thenReturn(trainings);

        mockMvc.perform(post("/api/v1/trainees/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainings").isArray())
                .andExpect(jsonPath("$.trainings[0].trainingName").value("Morning Cardio"));
        verify(trainingService, times(1)).getTraineeTrainingList(request);
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD)
    void getTraineeTrainings_Return200AndTrainings_OnlyRequiredParametersProvidedAndValid() throws Exception {
        GetTraineeTrainingsRequest request = new GetTraineeTrainingsRequest(
                TRAINEE_USERNAME, null, null, null, null
        );
        Trainings trainings = new Trainings(List.of(getTrainingSummary()));
        when(trainingService.getTraineeTrainingList(request)).thenReturn(trainings);

        mockMvc.perform(post("/api/v1/trainees/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainings").isArray())
                .andExpect(jsonPath("$.trainings[0].trainingName").value("Morning Cardio"));
        verify(trainingService, times(1)).getTraineeTrainingList(request);
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD)
    void getTraineeTrainings_Return200AndTrainings_ToDateAbsent() throws Exception {
        GetTraineeTrainingsRequest request = new GetTraineeTrainingsRequest(
                TRAINEE_USERNAME, LocalDate.of(2026, 1, 1), null, null, null
        );
        Trainings trainings = new Trainings(List.of(getTrainingSummary()));
        when(trainingService.getTraineeTrainingList(request)).thenReturn(trainings);

        mockMvc.perform(post("/api/v1/trainees/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainings").isArray())
                .andExpect(jsonPath("$.trainings[0].trainingName").value("Morning Cardio"));
        verify(trainingService, times(1)).getTraineeTrainingList(request);
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD)
    void getTraineeTrainings_Return200AndTrainings_FromDateAbsent() throws Exception {
        GetTraineeTrainingsRequest request = new GetTraineeTrainingsRequest(
                TRAINEE_USERNAME, null, LocalDate.of(2026, 12, 1), null, null
        );
        Trainings trainings = new Trainings(List.of(getTrainingSummary()));
        when(trainingService.getTraineeTrainingList(request)).thenReturn(trainings);

        mockMvc.perform(post("/api/v1/trainees/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainings").isArray())
                .andExpect(jsonPath("$.trainings[0].trainingName").value("Morning Cardio"));
        verify(trainingService, times(1)).getTraineeTrainingList(request);
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD)
    void getTraineeTrainings_Return400AndProblemDetail_UsernameIsBlank() throws Exception {
        GetTraineeTrainingsRequest request = new GetTraineeTrainingsRequest(
                "  ", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), TRAINER_USERNAME, TrainingType.YOGA
        );
        mockMvc.perform(post("/api/v1/trainees/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());;
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD)
    void getTraineeTrainings_Return400AndProblemDetail_FromDateAfterToDate() throws Exception {
        GetTraineeTrainingsRequest request = new GetTraineeTrainingsRequest(
                TRAINEE_USERNAME, LocalDate.of(2026, 12, 31),
                LocalDate.of(2026, 1, 31), TRAINER_USERNAME, TrainingType.YOGA
        );
        mockMvc.perform(post("/api/v1/trainees/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());;
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD)
    void getTraineeTrainings_Return400AndProblemDetail_TrainerNameIsTooLong() throws Exception {
        String trainerName = "A".repeat(51);
        GetTraineeTrainingsRequest request = new GetTraineeTrainingsRequest(
                TRAINEE_USERNAME, LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), trainerName, TrainingType.YOGA
        );
        mockMvc.perform(post("/api/v1/trainees/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());;
    }

    @Test
    @WithAnonymousUser
    void getTraineeTrainings_Return401AndProblemDetail_UserIsUnauthenticated() throws Exception {
        GetTraineeTrainingsRequest request = new GetTraineeTrainingsRequest(
                TRAINEE_USERNAME, LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), "Doe", TrainingType.YOGA
        );
        mockMvc.perform(post("/api/v1/trainees/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication Failure"))
                .andExpect(jsonPath("$.detail").value("Authentication failed during accessing the resource"));
    }
}
