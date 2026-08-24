package org.example.crm.trainer;

import org.example.crm.config.SecurityConfig;
import org.example.crm.exception.EntityNotFoundException;
import org.example.crm.security.service.JwtService;
import org.example.crm.security.service.OwnershipVerifier;
import org.example.crm.security.service.TokenBlackListService;
import org.example.crm.trainer.controller.TrainerController;
import org.example.crm.trainer.controller.request.CreateTrainerRequest;
import org.example.crm.trainer.controller.request.GetTrainerTrainingsRequest;
import org.example.crm.trainer.controller.request.UpdateTrainerRequest;
import org.example.crm.trainer.controller.response.TrainerSummary;
import org.example.crm.trainer.controller.response.Trainers;
import org.example.crm.trainer.service.TrainerService;
import org.example.crm.training.controller.response.Trainings;
import org.example.crm.training.service.TrainingService;
import org.example.crm.trainingType.dto.TrainingType;
import org.example.crm.user.controller.dto.FullName;
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
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.crm.TestUtils.*;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TrainerController.class)
@Import(SecurityConfig.class)
class TrainerControllerIT {

    private static final UserDetails USER_DETAILS = getTrainerUserDetails();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private TrainerService trainerService;

    @MockitoBean
    private TrainingService trainingService;

    @MockitoBean
    private OwnershipVerifier ownershipVerifier;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private TokenBlackListService tokenBlackListService;

    @Test
    void registerTrainer_Return201AndTrainerSummary_AllParametersProvidedAndValid() throws Exception {
        CreateTrainerRequest request = new CreateTrainerRequest(
                new FullName("John", "Doe"), TrainingType.YOGA
        );
        TrainerSummary expectedTrainer = getTrainerSummary();
        when(trainerService.create(request)).thenReturn(expectedTrainer);

        MvcResult result = mockMvc.perform(post("/api/v1/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        TrainerSummary response = jsonMapper.readValue(result.getResponse().getContentAsString(), TrainerSummary.class);
        assertThat(response.id()).isEqualTo(expectedTrainer.id());
        assertThat(response.profile().username()).isEqualTo(expectedTrainer.profile().username());
        assertThat(response.specialization()).isEqualTo(expectedTrainer.specialization());
        verify(trainerService, times(1)).create(request);
    }

    @Test
    void registerTrainer_Return400AndProblemDetail_FullNameIsNull() throws Exception {
        CreateTrainerRequest request = new CreateTrainerRequest(
                null, TrainingType.YOGA
        );

        mockMvc.perform(post("/api/v1/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void registerTrainer_Return400AndProblemDetail_FirstNameIsBlank() throws Exception {
        CreateTrainerRequest request = new CreateTrainerRequest(
                new FullName("  ", "Doe"), TrainingType.YOGA
        );

        mockMvc.perform(post("/api/v1/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void registerTrainer_Return400AndProblemDetail_LastNameIsBlank() throws Exception {
        CreateTrainerRequest request = new CreateTrainerRequest(
                new FullName("John", ""), TrainingType.YOGA
        );

        mockMvc.perform(post("/api/v1/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
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
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD, roles = {"TRAINER"})
    void getTrainer_Return200AndTrainerSummary_IdIsValid() throws Exception {
        TrainerSummary expectedTrainer = getTrainerSummary();
        when(trainerService.getById(TRAINER_ID)).thenReturn(expectedTrainer);

        MvcResult result = mockMvc.perform(get("/api/v1/trainers/{id}", TRAINER_ID))
                .andExpect(status().isOk())
                .andReturn();

        TrainerSummary response = jsonMapper.readValue(result.getResponse().getContentAsString(), TrainerSummary.class);
        assertThat(response.id()).isEqualTo(expectedTrainer.id());
        assertThat(response.profile().username()).isEqualTo(expectedTrainer.profile().username());
        assertThat(response.specialization()).isEqualTo(expectedTrainer.specialization());
        verify(ownershipVerifier, times(1)).verifyOwnership(TRAINER_ID, USER_DETAILS, OwnershipVerifier.ResourceType.TRAINER);
        verify(trainerService, times(1)).getById(TRAINER_ID);
    }

    @Test
    @WithAnonymousUser
    void getTrainer_Return401AndProblemDetail_UserIsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/trainers/{id}", TRAINER_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication Failure"))
                .andExpect(jsonPath("$.detail").value("Authentication token is missing"));
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD, roles = {"TRAINER"})
    void getTrainer_Return404AndProblemDetail_TrainerNotFound() throws Exception {
        Long id = 99L;
        when(trainerService.getById(id))
                .thenThrow(new EntityNotFoundException("Trainer not found"));

        mockMvc.perform(get("/api/v1/trainers/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Entity Not Found"));
        verify(ownershipVerifier, times(1)).verifyOwnership(id, USER_DETAILS, OwnershipVerifier.ResourceType.TRAINER);
        verify(trainerService, times(1)).getById(id);
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD , roles = {"TRAINER"})
    void updateTrainer_Return200AndTrainerSummary_AllParametersProvidedAndValid() throws Exception {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                new FullName("John", "Doe"), TrainingType.YOGA);
        TrainerSummary expectedTrainer = getTrainerSummary();
        when(trainerService.update(TRAINER_ID, request)).thenReturn(expectedTrainer);

        MvcResult result = mockMvc.perform(put("/api/v1/trainers/{id}", TRAINER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        TrainerSummary response = jsonMapper.readValue(result.getResponse().getContentAsString(), TrainerSummary.class);
        assertThat(response.id()).isEqualTo(expectedTrainer.id());
        assertThat(response.profile().username()).isEqualTo(expectedTrainer.profile().username());
        assertThat(response.specialization()).isEqualTo(expectedTrainer.specialization());
        verify(ownershipVerifier, times(1)).verifyOwnership(TRAINER_ID, USER_DETAILS, OwnershipVerifier.ResourceType.TRAINER);
        verify(trainerService, times(1)).update(TRAINER_ID, request);
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD , roles = {"TRAINER"})
    void updateTrainer_Return400AndProblemDetail_FullNameIsNull() throws Exception {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                null, TrainingType.YOGA
        );

        mockMvc.perform(put("/api/v1/trainers/{id}", TRAINER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields", hasKey("fullName")));
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD , roles = {"TRAINER"})
    void updateTrainer_Return400AndProblemDetail_FirstNameIsBlank() throws Exception {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                new FullName("  ", "Doe"), TrainingType.YOGA
        );

        mockMvc.perform(put("/api/v1/trainers/{id}", TRAINER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD , roles = {"TRAINER"})
    void updateTrainer_Return400AndProblemDetail_LastNameIsBlank() throws Exception {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                new FullName("John", ""), TrainingType.YOGA
        );

        mockMvc.perform(put("/api/v1/trainers/{id}", TRAINER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD , roles = {"TRAINER"})
    void updateTrainer_Return400AndProblemDetail_SpecializationIsNull() throws Exception {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                new FullName("John", "Doe"), null
        );

        mockMvc.perform(put("/api/v1/trainers/{id}", TRAINER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    @WithAnonymousUser
    void updateTrainer_Return401AndProblemDetail_UserIsUnauthenticated() throws Exception {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                new FullName("John", "Doe"), TrainingType.YOGA);

        mockMvc.perform(put("/api/v1/trainers/{id}", TRAINER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication Failure"))
                .andExpect(jsonPath("$.detail").value("Authentication token is missing"));
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void updateTrainer_Return403AndProblemDetail_UserRoleIsInvalid() throws Exception {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                new FullName("John", "Doe"), TrainingType.YOGA);

        mockMvc.perform(put("/api/v1/trainers/{id}", TRAINER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Authorization Failure"))
                .andExpect(jsonPath("$.detail").value("Not enough rights to access the resource"));
        verify(trainerService, never()).update(anyLong(), any());
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD , roles = {"TRAINER"})
    void updateTrainer_Return404AndProblemDetail_TrainerNotFound() throws Exception {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                new FullName("John", "Doe"), TrainingType.YOGA);
        when(trainerService.update(TRAINER_ID, request))
                .thenThrow(new EntityNotFoundException("Trainer not found"));

        mockMvc.perform(put("/api/v1/trainers/{id}", TRAINER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Entity Not Found"));
        verify(ownershipVerifier, times(1)).verifyOwnership(TRAINER_ID, USER_DETAILS, OwnershipVerifier.ResourceType.TRAINER);
        verify(trainerService, times(1)).update(TRAINER_ID, request);
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD, roles = {"TRAINEE"})
    void getNotAssignedTrainers_Return200AndTrainers_RequestIsValid() throws Exception {
        Trainers trainers = new Trainers(List.of(getTrainerSummary()));
        when(trainerService.getUnassignedTrainersByTraineeList(TRAINEE_USERNAME))
                .thenReturn(trainers);

        MvcResult result = mockMvc.perform(get("/api/v1/trainers/not-assigned")
                        .param("trainee-username", TRAINEE_USERNAME))
                .andExpect(status().isOk())
                .andReturn();

        Trainers response = jsonMapper.readValue(result.getResponse().getContentAsString(), Trainers.class);
        assertThat(response.trainers()).hasSize(1);
        assertThat(response.trainers().get(0).profile().username()).isEqualTo(TRAINER_USERNAME);
        verify(ownershipVerifier, times(1)).verifyOwnership(TRAINEE_USERNAME, getTraineeUserDetails());
        verify(trainerService, times(1)).getUnassignedTrainersByTraineeList(TRAINEE_USERNAME);
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD, roles = {"TRAINEE"})
    void getNotAssignedTrainers_Return400AndProblemDetail_TraineeUsernameIsBlank() throws Exception {
        mockMvc.perform(get("/api/v1/trainers/not-assigned")
                        .param("trainee-username", "  "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.violations").isNotEmpty());
    }

    @Test
    @WithAnonymousUser
    void getNotAssignedTrainers_Return401AndProblemDetail_UserIsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/trainers/not-assigned")
                        .param("trainee-username", TRAINEE_USERNAME))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication Failure"))
                .andExpect(jsonPath("$.detail").value("Authentication token is missing"));
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD , roles = {"TRAINER"})
    void changeTrainerActivity_Return200_RequestIsValid() throws Exception {
        mockMvc.perform(patch("/api/v1/trainers/{id}/profile/active-status/change", TRAINER_ID))
                .andExpect(status().isOk());
        verify(ownershipVerifier, times(1)).verifyOwnership(TRAINER_ID, USER_DETAILS, OwnershipVerifier.ResourceType.TRAINER);
        verify(trainerService, times(1)).changeActivity(TRAINER_ID);
    }

    @Test
    @WithAnonymousUser
    void changeTrainerActivity_Return401AndProblemDetail_UserIsUnauthenticated() throws Exception {
        mockMvc.perform(patch("/api/v1/trainers/{id}/profile/active-status/change", TRAINER_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication Failure"))
                .andExpect(jsonPath("$.detail").value("Authentication token is missing"));
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD, roles = {"TRAINEE"})
    void changeTrainerActivity_Return403AndProblemDetail_UserRoleIsInvalid() throws Exception {
        mockMvc.perform(patch("/api/v1/trainers/{id}/profile/active-status/change", TRAINER_ID))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Authorization Failure"))
                .andExpect(jsonPath("$.detail").value("Not enough rights to access the resource"));
        verify(trainerService, never()).changeActivity(anyLong());
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD , roles = {"TRAINER"})
    void changeTrainerActivity_Return404AndProblemDetail_TraineeNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Trainer not found"))
                .when(trainerService).changeActivity(TRAINER_ID);

        mockMvc.perform(patch("/api/v1/trainers/{id}/profile/active-status/change", TRAINER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Entity Not Found"));
        verify(ownershipVerifier, times(1)).verifyOwnership(TRAINER_ID, USER_DETAILS, OwnershipVerifier.ResourceType.TRAINER);
        verify(trainerService, times(1)).changeActivity(TRAINER_ID);
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD, roles = {"TRAINER"})
    void getTrainerTrainings_Return200AndTrainings_AllParametersProvidedAndValid() throws Exception {
        GetTrainerTrainingsRequest request = new GetTrainerTrainingsRequest(
                TRAINER_USERNAME, LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), TRAINEE_USERNAME
        );
        Trainings trainings = new Trainings(List.of(getTrainingSummary()));
        when(trainingService.getTrainerTrainingList(request)).thenReturn(trainings);

        MvcResult result = mockMvc.perform(post("/api/v1/trainers/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        Trainings response = jsonMapper.readValue(result.getResponse().getContentAsString(), Trainings.class);
        assertThat(response.trainings()).hasSize(1);
        assertThat(response.trainings().get(0).trainingName()).isEqualTo("Morning Cardio");
        verify(ownershipVerifier, times(1)).verifyOwnership(TRAINER_USERNAME, USER_DETAILS);
        verify(trainingService, times(1)).getTrainerTrainingList(request);
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD, roles = {"TRAINER"})
    void getTrainerTrainings_Return200AndTrainings_OnlyRequiredParametersProvidedAndValid() throws Exception {
        GetTrainerTrainingsRequest request = new GetTrainerTrainingsRequest(
                TRAINER_USERNAME, null, null, null
        );
        Trainings trainings = new Trainings(List.of(getTrainingSummary()));
        when(trainingService.getTrainerTrainingList(request)).thenReturn(trainings);

        mockMvc.perform(post("/api/v1/trainers/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainings").isArray())
                .andExpect(jsonPath("$.trainings[0].trainingName").value("Morning Cardio"));
        verify(ownershipVerifier, times(1)).verifyOwnership(TRAINER_USERNAME, USER_DETAILS);
        verify(trainingService, times(1)).getTrainerTrainingList(request);
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD, roles = {"TRAINER"})
    void getTrainerTrainings_Return200AndTrainings_ToDateAbsent() throws Exception {
        GetTrainerTrainingsRequest request = new GetTrainerTrainingsRequest(
                TRAINER_USERNAME, LocalDate.of(2026, 1, 1), null, null
        );
        Trainings trainings = new Trainings(List.of(getTrainingSummary()));
        when(trainingService.getTrainerTrainingList(request)).thenReturn(trainings);

        mockMvc.perform(post("/api/v1/trainers/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainings").isArray())
                .andExpect(jsonPath("$.trainings[0].trainingName").value("Morning Cardio"));
        verify(ownershipVerifier, times(1)).verifyOwnership(TRAINER_USERNAME, USER_DETAILS);
        verify(trainingService, times(1)).getTrainerTrainingList(request);
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD, roles = {"TRAINER"})
    void getTrainerTrainings_Return200AndTrainings_FromDateAbsent() throws Exception {
        GetTrainerTrainingsRequest request = new GetTrainerTrainingsRequest(
                TRAINER_USERNAME, null, LocalDate.of(2026, 12, 1), null
        );
        Trainings trainings = new Trainings(List.of(getTrainingSummary()));
        when(trainingService.getTrainerTrainingList(request)).thenReturn(trainings);

        mockMvc.perform(post("/api/v1/trainers/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainings").isArray())
                .andExpect(jsonPath("$.trainings[0].trainingName").value("Morning Cardio"));
        verify(ownershipVerifier, times(1)).verifyOwnership(TRAINER_USERNAME, USER_DETAILS);
        verify(trainingService, times(1)).getTrainerTrainingList(request);
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD, roles = "TRAINER")
    void getTrainerTrainings_Return400AndProblemDetail_UsernameIsBlank() throws Exception {
        GetTrainerTrainingsRequest request = new GetTrainerTrainingsRequest(
                "  ", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), TRAINER_USERNAME
        );
        mockMvc.perform(post("/api/v1/trainers/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD, roles = "TRAINER")
    void getTrainerTrainings_Return400AndProblemDetail_FromDateAfterToDate() throws Exception {
        GetTrainerTrainingsRequest request = new GetTrainerTrainingsRequest(
                TRAINEE_USERNAME, LocalDate.of(2026, 12, 31),
                LocalDate.of(2026, 1, 31), TRAINER_USERNAME
        );
        mockMvc.perform(post("/api/v1/trainers/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, password = TRAINER_PASSWORD, roles = "TRAINER")
    void getTrainerTrainings_Return400AndProblemDetail_TraineeNameIsTooLong() throws Exception {
        String traineeName = "A".repeat(51);
        GetTrainerTrainingsRequest request = new GetTrainerTrainingsRequest(
                TRAINEE_USERNAME, LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), traineeName
        );
        mockMvc.perform(post("/api/v1/trainers/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    @WithAnonymousUser
    void getTrainerTrainings_Return401AndProblemDetail_UserIsUnauthenticated() throws Exception {
        GetTrainerTrainingsRequest request = new GetTrainerTrainingsRequest(
                TRAINEE_USERNAME, LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), "Doe"
        );
        mockMvc.perform(post("/api/v1/trainers/trainings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication Failure"))
                .andExpect(jsonPath("$.detail").value("Authentication token is missing"));
    }
}
