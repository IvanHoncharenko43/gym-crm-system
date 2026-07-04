package org.example.shared;

import org.example.TestUtils;
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
import org.example.utils.GymMapper;
import org.example.utils.PasswordGenerator;
import org.example.utils.UsernameGenerator;
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

        when(usernameGenerator.generate(TestUtils.FIRST_NAME, TestUtils.LAST_NAME)).thenReturn(TestUtils.TRAINEE_USERNAME);
        when(passwordGenerator.generate()).thenReturn(TestUtils.TRAINEE_PASSWORD);

        TraineeEntity trainee = gymMapper.toTraineeEntity(request);
        assertNotNull(trainee);
        assertEquals(request.fullName().firstName(), trainee.getFirstName());
        assertEquals(request.fullName().lastName(), trainee.getLastName());
        assertEquals(request.dateOfBirth(), trainee.getDateOfBirth());
        assertEquals(request.address(), trainee.getAddress());
        assertTrue(trainee.isActive());

        verify(usernameGenerator, times(1)).generate(TestUtils.FIRST_NAME, TestUtils.LAST_NAME);
        verify(passwordGenerator, times(1)).generate();
    }

    @Test
    void toTrainee_MapCorrectly_FromUpdateRequest() {
        UpdateTraineeRequest request = TestUtils.getUpdateTraineeRequest();
        String username = TestUtils.TRAINEE_USERNAME;
        String password = TestUtils.TRAINEE_PASSWORD;

        TraineeEntity trainee = gymMapper.toTraineeEntity(request, username, password);

        assertNotNull(trainee);
        assertEquals(request.id(), trainee.getId());
        assertEquals(request.fullName().firstName(), trainee.getFirstName());
        assertEquals(request.fullName().lastName(), trainee.getLastName());
        assertEquals(username, trainee.getUsername());
        assertEquals(password, trainee.getPassword());
        assertEquals(request.isActive(), trainee.isActive());
        assertEquals(request.dateOfBirth(), trainee.getDateOfBirth());
        assertEquals(request.address(), trainee.getAddress());
    }

    @Test
    void toTraineeSummary_MapAndNestProfileCorrectly_FromTrainee() {
        TraineeEntity trainee = TestUtils.getTrainee();

        TraineeSummary response = gymMapper.toTraineeSummary(trainee);

        assertNotNull(response);
        assertEquals(trainee.getId(), response.id());
        assertEquals(trainee.getUsername(), response.profile().username());
        assertEquals(trainee.getDateOfBirth(), response.dateOfBirth());
        assertEquals(trainee.getAddress(), response.address());
    }

    @Test
    void toTrainer_MapCorrectly_FromCreateRequest() {
        CreateTrainerRequest request = TestUtils.getCreateTrainerRequest();

        when(usernameGenerator.generate(TestUtils.FIRST_NAME, TestUtils.LAST_NAME)).thenReturn(TestUtils.TRAINER_USERNAME);
        when(passwordGenerator.generate()).thenReturn(TestUtils.TRAINER_PASSWORD);

        TrainerEntity trainer = gymMapper.toTrainerEntity(request);

        assertNotNull(trainer);
        assertEquals(request.fullName().firstName(), trainer.getFirstName());
        assertEquals(request.fullName().lastName(), trainer.getLastName());
        assertEquals(request.specialization(), trainer.getSpecialization());
        assertTrue(trainer.isActive());

        verify(usernameGenerator, times(1)).generate(TestUtils.FIRST_NAME, TestUtils.LAST_NAME);
        verify(passwordGenerator, times(1)).generate();
    }

    @Test
    void toTrainer_MapCorrectly_FromUpdateRequest() {
        UpdateTrainerRequest request = TestUtils.getUpdateTrainerRequest();
        String username = TestUtils.TRAINER_USERNAME;
        String password = TestUtils.TRAINER_PASSWORD;

        TrainerEntity trainer = gymMapper.toTrainerEntity(request, username, password);

        assertNotNull(trainer);
        assertEquals(request.id(), trainer.getId());
        assertEquals(request.fullName().firstName(), trainer.getFirstName());
        assertEquals(request.fullName().lastName(), trainer.getLastName());
        assertEquals(request.specialization(), trainer.getSpecialization());
        assertEquals(request.isActive(), trainer.isActive());
    }

    @Test
    void toTrainerSummary_MapAndNestProfileCorrectly_FromTrainer() {
        TrainerEntity trainer = TestUtils.getTrainer();

        TrainerSummary response = gymMapper.toTrainerSummary(trainer);

        assertNotNull(response);
        assertEquals(trainer.getId(), response.id());
        assertEquals(trainer.getUsername(), response.profile().username());
        assertEquals(trainer.getSpecialization(), response.specialization());
    }

    @Test
    void toTraining_MapFromRequestAndEntitiesCorrectly_FromCreateRequestAndTraineeAndTrainer() {
        CreateTrainingRequest request = TestUtils.getCreateTrainingRequest();
        TraineeEntity trainee = TestUtils.getTrainee();
        TrainerEntity trainer = TestUtils.getTrainer();

        TrainingEntity training = gymMapper.toTraining(request, trainee, trainer);

        assertNotNull(training);;
        assertEquals(request.traineeId(), training.getTraineeId());
        assertEquals(request.trainerId(), training.getTrainerId());
        assertEquals(request.trainingName(), training.getTrainingName());
        assertEquals(request.trainingDate(), training.getTrainingDate());
        assertEquals(request.durationMinutes(), training.getDurationMinutes());
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
        assertEquals(training.getTrainingType(), response.trainingType());
        assertEquals(training.getTrainingDate(), response.trainingDate());
        assertEquals(training.getDurationMinutes(), response.durationMinutes());

        assertNotNull(response.trainer());
        assertEquals(trainer.getId(), response.id());
        assertEquals(trainer.getUsername(), response.trainer().profile().username());
        assertEquals(trainer.getSpecialization(), response.trainer().specialization());

        assertNotNull(response.trainee());
        assertEquals(trainee.getId(), response.id());
        assertEquals(trainee.getUsername(), response.trainee().profile().username());
        assertEquals(trainee.getDateOfBirth(), response.trainee().dateOfBirth());
        assertEquals(trainee.getAddress(), response.trainee().address());
    }
}
