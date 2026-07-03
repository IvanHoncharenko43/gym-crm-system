package org.example.shared;

import org.example.TestUtils;
import org.example.trainee.CreateTraineeRequest;
import org.example.trainee.TraineeEntity;
import org.example.trainee.TraineeSummary;
import org.example.trainee.UpdateTraineeRequest;
import org.example.trainer.CreateTrainerRequest;
import org.example.trainer.TrainerEntity;
import org.example.trainer.TrainerSummary;
import org.example.trainer.UpdateTrainerRequest;
import org.example.training.CreateTrainingRequest;
import org.example.training.TrainingEntity;
import org.example.training.TrainingSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GymMapperTest {

    private GymMapper gymMapper;

    @BeforeEach
    void setUp() {
        gymMapper = new GymMapper();
    }

    @Test
    void toTrainee_MapCorrectly_FromCreateRequest() {
        CreateTraineeRequest request = TestUtils.getCreateTraineeRequest();

        TraineeEntity trainee = gymMapper.toTraineeEntity(request);
        assertNotNull(trainee);
        assertEquals(request.fullName().firstName(), trainee.getFirstName());
        assertEquals(request.fullName().lastName(), trainee.getLastName());
        assertEquals(request.dateOfBirth(), trainee.getDateOfBirth());
        assertEquals(request.address(), trainee.getAddress());
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

    //    @Test
//    void toTrainee_ThrowNullPointerException_NullCreateRequest() {
//        NullPointerException exception = assertThrows(NullPointerException.class,
//                () -> gymMapper.toTrainee((CreateTraineeRequest) null));
//        assertEquals("Create request cannot be null", exception.getMessage());
//    }
//
//    @Test
//    void toTraineeResponse_ThrowNullPointerException_NullTrainee() {
//        NullPointerException exception = assertThrows(NullPointerException.class,
//                () -> gymMapper.toTraineeResponse(null));
//        assertEquals("Trainee entity cannot be null", exception.getMessage());
//    }

    @Test
    void toTrainer_MapCorrectly_FromCreateRequest() {
        CreateTrainerRequest request = TestUtils.getCreateTrainerRequest();

        TrainerEntity trainer = gymMapper.toTrainerEntity(request);

        assertNotNull(trainer);
        assertEquals(request.fullName().firstName(), trainer.getFirstName());
        assertEquals(request.fullName().lastName(), trainer.getLastName());
        assertEquals(request.specialization(), trainer.getSpecialization());
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

//    @Test
//    void toTraining_ThrowNullPointerException_NullRequest() {
//        NullPointerException exception = assertThrows(NullPointerException.class,
//                () -> gymMapper.toTraining(null, new Trainee(), new Trainer()));
//        assertEquals("Create request cannot be null", exception.getMessage());
//    }
//
//    @Test
//    void toTrainingResponse_ThrowNullPointerException_NullTraining() {
//        NullPointerException exception = assertThrows(NullPointerException.class,
//                () -> gymMapper.toTrainingResponse(null, new Trainee(), new Trainer()));
//        assertEquals("Training entity cannot be null", exception.getMessage());
//    }
}
