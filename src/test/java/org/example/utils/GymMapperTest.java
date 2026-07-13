package org.example.utils;

import org.example.TestUtils;
import org.example.core.service.GymMapper;
import org.example.trainee.dto.CreateTraineeRequest;
import org.example.trainee.repository.TraineeEntity;
import org.example.trainee.dto.TraineeSummary;
import org.example.trainee.dto.UpdateTraineeRequest;
import org.example.trainer.dto.CreateTrainerRequest;
import org.example.trainer.repository.TrainerEntity;
import org.example.trainer.dto.TrainerSummary;
import org.example.trainer.dto.UpdateTrainerRequest;
import org.example.training.dto.CreateTrainingRequest;
import org.example.training.repository.TrainingEntity;
import org.example.training.dto.TrainingSummary;
import org.example.training.repository.TrainingTypeEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GymMapperTest {

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

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
    }

    @Test
    void toTrainee_MapCorrectly_FromUpdateRequest() {
        UpdateTraineeRequest request = TestUtils.getUpdateTraineeRequest();
        TraineeEntity trainee = TestUtils.getTrainee();

        trainee = gymMapper.toTraineeEntity(request, trainee);

        assertNotNull(trainee);
        assertEquals(request.id(), trainee.getId());
        assertEquals(request.fullName().firstName(), trainee.getUser().getFirstName());
        assertEquals(request.fullName().lastName(), trainee.getUser().getLastName());
        assertEquals(request.credentials().username(), trainee.getUser().getUsername());
        assertEquals(request.credentials().password(), trainee.getUser().getPassword());
        assertEquals(request.dateOfBirth(), trainee.getDateOfBirth());
        assertEquals(request.address(), trainee.getAddress());
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
        assertEquals(request.specialization().id(), trainer.getSpecialization().getId());
        assertEquals(request.specialization().trainingTypeName(), trainer.getSpecialization().getTrainingTypeName());

        verify(usernameGenerator, times(1))
                .generate(TestUtils.FIRST_NAME, TestUtils.LAST_NAME, TestUtils.getExistingUsernamesSet());
        verify(passwordGenerator, times(1)).generate();
    }

    @Test
    void toTrainer_MapCorrectly_FromUpdateRequest() {
        UpdateTrainerRequest request = TestUtils.getUpdateTrainerRequest();
        TrainerEntity trainer = TestUtils.getTrainer();
        TrainingTypeEntity trainingType = TestUtils.getTrainingType();

        trainer = gymMapper.toTrainerEntity(request, trainer, trainingType);

        assertNotNull(trainer);
        assertEquals(request.id(), trainer.getId());
        assertEquals(request.fullName().firstName(), trainer.getUser().getFirstName());
        assertEquals(request.fullName().lastName(), trainer.getUser().getLastName());

        assertNotNull(trainer.getSpecialization());
        assertEquals(request.specialization().id(), trainer.getSpecialization().getId());
        assertEquals(request.specialization().trainingTypeName(), trainer.getSpecialization().getTrainingTypeName());
    }

    @Test
    void toTrainerSummary_MapAndNestProfileCorrectly_FromTrainer() {
        TrainerEntity trainer = TestUtils.getTrainer();

        TrainerSummary response = gymMapper.toTrainerSummary(trainer);

        assertNotNull(response);
        assertEquals(trainer.getId(), response.id());
        assertEquals(trainer.getUser().getUsername(), response.profile().username());

        assertNotNull(response.specialization());
        assertEquals(trainer.getSpecialization().getId(), response.specialization().id());
        assertEquals(trainer.getSpecialization().getTrainingTypeName(), response.specialization().trainingTypeName());
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
        assertEquals(training.getTrainingType().getId(), response.trainingType().id());
        assertEquals(training.getTrainingType().getTrainingTypeName(), response.trainingType().trainingTypeName());

        assertNotNull(response.trainer());
        assertEquals(trainer.getId(), response.id());
        assertEquals(trainer.getUser().getUsername(), response.trainer().profile().username());

        assertNotNull(response.trainer().specialization());
        assertEquals(trainer.getSpecialization().getId(), response.trainer().specialization().id());
        assertEquals(trainer.getSpecialization().getTrainingTypeName(), response.trainer().specialization().trainingTypeName());

        assertNotNull(response.trainee());
        assertEquals(trainee.getId(), response.id());
        assertEquals(trainee.getUser().getUsername(), response.trainee().profile().username());
        assertEquals(trainee.getDateOfBirth(), response.trainee().dateOfBirth());
        assertEquals(trainee.getAddress(), response.trainee().address());
    }
}
