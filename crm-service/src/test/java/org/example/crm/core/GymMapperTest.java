package org.example.crm.core;

import org.example.crm.TestUtils;
import org.example.crm.core.dto.ActionType;
import org.example.crm.core.service.GymMapper;
import org.example.crm.trainee.controller.request.CreateTraineeRequest;
import org.example.crm.trainee.repository.TraineeEntity;
import org.example.crm.trainee.controller.response.TraineeSummary;
import org.example.crm.trainee.controller.request.UpdateTraineeRequest;
import org.example.crm.trainer.controller.request.CreateTrainerRequest;
import org.example.crm.trainer.client.request.TrainerUpdateWorkloadClientRequest;
import org.example.crm.trainer.repository.TrainerEntity;
import org.example.crm.trainer.controller.response.TrainerSummary;
import org.example.crm.trainer.controller.request.UpdateTrainerRequest;
import org.example.crm.training.controller.request.CreateTrainingRequest;
import org.example.crm.training.repository.TrainingEntity;
import org.example.crm.training.controller.response.TrainingSummary;
import org.example.crm.trainingType.repository.TrainingTypeEntity;
import org.example.crm.utils.PasswordGenerator;
import org.example.crm.utils.UsernameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GymMapperTest {

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private GymMapper gymMapper;

    @Test
    void toTrainee_MapCorrectly_FromCreateRequest() {
        CreateTraineeRequest request = TestUtils.getCreateTraineeRequest();

        when(usernameGenerator.generate(TestUtils.FIRST_NAME, TestUtils.LAST_NAME, TestUtils.getExistingUsernamesSet()))
                .thenReturn(TestUtils.TRAINEE_USERNAME);
        when(passwordGenerator.generate()).thenReturn(TestUtils.TRAINEE_PASSWORD);

        TraineeEntity trainee = gymMapper.toTraineeEntity(request, TestUtils.getExistingUsernamesSet());
        assertNotNull(trainee);
        assertEquals(request.fullName().firstName(), trainee.getUser().getFirstName());
        assertEquals(request.fullName().lastName(), trainee.getUser().getLastName());
        assertEquals(request.dateOfBirth(), trainee.getDateOfBirth());
        assertEquals(request.address(), trainee.getAddress());
        assertTrue(trainee.getUser().getIsActive());

        verify(usernameGenerator, times(1))
                .generate(TestUtils.FIRST_NAME, TestUtils.LAST_NAME, TestUtils.getExistingUsernamesSet());
        verify(passwordGenerator, times(1)).generate();
        verify(passwordEncoder, times(1)).encode(TestUtils.TRAINEE_PASSWORD);
    }

    @Test
    void toTrainee_MapCorrectly_FromUpdateRequest() {
        UpdateTraineeRequest request = TestUtils.getUpdateTraineeRequest();
        TraineeEntity expectedTrainee = TestUtils.getTrainee();

        TraineeEntity actualTrainee = gymMapper.toTraineeEntity(request, expectedTrainee);

        assertNotNull(actualTrainee);
        assertEquals(expectedTrainee.getId(), actualTrainee.getId());
        assertEquals(request.fullName().firstName(), actualTrainee.getUser().getFirstName());
        assertEquals(request.fullName().lastName(), actualTrainee.getUser().getLastName());
        assertEquals(expectedTrainee.getUser().getUsername(), actualTrainee.getUser().getUsername());
        assertEquals(expectedTrainee.getUser().getPassword(), actualTrainee.getUser().getPassword());
        assertEquals(request.dateOfBirth(), actualTrainee.getDateOfBirth());
        assertEquals(request.address(), actualTrainee.getAddress());
    }

    @Test
    void toTraineeSummary_MapAndNestProfileCorrectly_FromTrainee() {
        TraineeEntity trainee = TestUtils.getTrainee();

        TraineeSummary response = gymMapper.toTraineeSummary(trainee);

        assertNotNull(response);
        assertEquals(trainee.getId(), response.id());
        assertEquals(trainee.getUser().getUsername(), response.profile().username());
        assertEquals(trainee.getDateOfBirth(), response.dateOfBirth());
        assertEquals(trainee.getAddress(), response.address());
    }

    @Test
    void toTrainer_MapCorrectly_FromCreateRequest() {
        CreateTrainerRequest request = TestUtils.getCreateTrainerRequest();
        TrainingTypeEntity trainingType = TestUtils.getTrainingType();

        when(usernameGenerator.generate(TestUtils.FIRST_NAME, TestUtils.LAST_NAME, TestUtils.getExistingUsernamesSet()))
                .thenReturn(TestUtils.TRAINER_USERNAME);
        when(passwordGenerator.generate()).thenReturn(TestUtils.TRAINER_PASSWORD);

        TrainerEntity trainer = gymMapper.toTrainerEntity(request, trainingType, TestUtils.getExistingUsernamesSet());

        assertNotNull(trainer);
        assertEquals(request.fullName().firstName(), trainer.getUser().getFirstName());
        assertEquals(request.fullName().lastName(), trainer.getUser().getLastName());
        assertTrue(trainer.getUser().getIsActive());

        assertNotNull(trainer.getSpecialization());
        assertEquals(request.specialization(), trainer.getSpecialization().getTrainingTypeName());

        verify(usernameGenerator, times(1))
                .generate(TestUtils.FIRST_NAME, TestUtils.LAST_NAME, TestUtils.getExistingUsernamesSet());
        verify(passwordGenerator, times(1)).generate();
        verify(passwordEncoder, times(1)).encode(TestUtils.TRAINER_PASSWORD);
    }

    @Test
    void toTrainer_MapCorrectly_FromUpdateRequest() {
        UpdateTrainerRequest request = TestUtils.getUpdateTrainerRequest();
        TrainerEntity expectedTrainer = TestUtils.getTrainer();
        TrainingTypeEntity trainingType = TestUtils.getTrainingType();

        TrainerEntity actualTrainer = gymMapper.toTrainerEntity(request, expectedTrainer, trainingType);

        assertNotNull(actualTrainer);
        assertEquals(expectedTrainer.getId(), actualTrainer.getId());
        assertEquals(request.fullName().firstName(), actualTrainer.getUser().getFirstName());
        assertEquals(request.fullName().lastName(), actualTrainer.getUser().getLastName());
        assertEquals(expectedTrainer.getUser().getUsername(), actualTrainer.getUser().getUsername());
        assertEquals(expectedTrainer.getUser().getPassword(), actualTrainer.getUser().getPassword());

        assertNotNull(actualTrainer.getSpecialization());
        assertEquals(request.specialization(), actualTrainer.getSpecialization().getTrainingTypeName());
    }

    @Test
    void toTrainerSummary_MapAndNestProfileCorrectly_FromTrainer() {
        TrainerEntity trainer = TestUtils.getTrainer();

        TrainerSummary response = gymMapper.toTrainerSummary(trainer);

        assertNotNull(response);
        assertEquals(trainer.getId(), response.id());
        assertEquals(trainer.getUser().getUsername(), response.profile().username());

        assertNotNull(response.specialization());
        assertEquals(trainer.getSpecialization().getTrainingTypeName(), response.specialization());
    }

    @Test
    void toTrainerUpdateWorkloadClientRequest_MapCorrectly_FromTrainerAndTrainingAndActionTypeAdd() {
        TrainerEntity trainer = TestUtils.getTrainer();
        TrainingEntity training = TestUtils.getTraining();

        TrainerUpdateWorkloadClientRequest request = gymMapper.toTrainerUpdateWorkloadClientRequest(trainer, training, ActionType.ADD);

        assertNotNull(request);
        assertEquals(trainer.getUser().getUsername(), request.username());
        assertEquals(trainer.getUser().getFirstName(), request.fullName().firstName());
        assertEquals(trainer.getUser().getLastName(), request.fullName().lastName());
        assertEquals(trainer.getUser().getIsActive(), request.isActive());
        assertEquals(training.getTrainingDate(), request.trainingDate());
        assertEquals(training.getDurationMinutes(), request.trainingSummaryDurationMinutes());
        assertEquals(ActionType.ADD, request.actionType());
    }

    @Test
    void toTrainerUpdateWorkloadClientRequest_MapCorrectly_FromTrainerAndTrainingAndActionTypeDelete() {
        TrainerEntity trainer = TestUtils.getTrainer();
        TrainingEntity training = TestUtils.getTraining();

        TrainerUpdateWorkloadClientRequest request = gymMapper.toTrainerUpdateWorkloadClientRequest(trainer, training, ActionType.DELETE);

        assertNotNull(request);
        assertEquals(trainer.getUser().getUsername(), request.username());
        assertEquals(trainer.getUser().getFirstName(), request.fullName().firstName());
        assertEquals(trainer.getUser().getLastName(), request.fullName().lastName());
        assertEquals(trainer.getUser().getIsActive(), request.isActive());
        assertEquals(training.getTrainingDate(), request.trainingDate());
        assertEquals(training.getDurationMinutes(), request.trainingSummaryDurationMinutes());
        assertEquals(ActionType.DELETE, request.actionType());
    }

    @Test
    void toTraining_MapFromRequestAndEntitiesCorrectly_FromCreateRequestAndTraineeAndTrainer() {
        CreateTrainingRequest request = TestUtils.getCreateTrainingRequest();
        TraineeEntity trainee = TestUtils.getTrainee();
        TrainerEntity trainer = TestUtils.getTrainer();

        TrainingEntity training = gymMapper.toTraining(request, trainee, trainer);

        assertNotNull(training);;
        assertEquals(request.trainingName(), training.getTrainingName());
        assertEquals(request.trainingDate(), training.getTrainingDate());
        assertEquals(request.durationMinutes(), training.getDurationMinutes());

        assertNotNull(training.getTrainingType());
        assertNotNull(training.getTrainee());
        assertNotNull(training.getTrainer());
    }

    @Test
    void toTrainingResponse_MapAndNestSummariesCorrectly_FromTrainingAndTraineeAndTrainer() {
        TrainingEntity training = TestUtils.getTraining();
        TraineeEntity trainee = TestUtils.getTrainee();
        TrainerEntity trainer = TestUtils.getTrainer();

        TrainingSummary response = gymMapper.toTrainingSummary(training, trainee, trainer);

        assertNotNull(response);
        assertEquals(training.getId(), response.id());
        assertEquals(training.getTrainingName(), response.trainingName());
        assertEquals(training.getTrainingDate(), response.trainingDate());
        assertEquals(training.getDurationMinutes(), response.durationMinutes());

        assertNotNull(response.trainingType());
        assertEquals(training.getTrainingType().getTrainingTypeName(), response.trainingType());

        assertNotNull(response.trainer());
        assertEquals(trainer.getId(), response.id());
        assertEquals(trainer.getUser().getUsername(), response.trainer().profile().username());

        assertNotNull(response.trainer().specialization());
        assertEquals(trainer.getSpecialization().getTrainingTypeName(), response.trainer().specialization());

        assertNotNull(response.trainee());
        assertEquals(trainee.getId(), response.id());
        assertEquals(trainee.getUser().getUsername(), response.trainee().profile().username());
        assertEquals(trainee.getDateOfBirth(), response.trainee().dateOfBirth());
        assertEquals(trainee.getAddress(), response.trainee().address());
    }
}
