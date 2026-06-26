package org.example.shared;

import org.example.trainee.*;
import org.example.trainer.*;
import org.example.training.CreateTrainingRequest;
import org.example.training.Training;
import org.example.training.TrainingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GymMapperTest {

    private GymMapper gymMapper;

    @BeforeEach
    void setUp() {
        gymMapper = new GymMapper();
    }

    @Test
    void toTrainee_MapCorrectly_FromCreateRequest() {
        CreateTraineeRequest request = mock(CreateTraineeRequest.class);
        when(request.firstName()).thenReturn("John");
        when(request.lastName()).thenReturn("Doe");
        when(request.dateOfBirth()).thenReturn(LocalDate.of(1995, 5, 15));
        when(request.address()).thenReturn("123 Main St");

        Trainee trainee = gymMapper.toTrainee(request);
        assertNotNull(trainee);
        assertEquals("John", trainee.getFirstName());
        assertEquals("Doe", trainee.getLastName());
        assertEquals(LocalDate.of(1995, 5, 15), trainee.getDateOfBirth());
        assertEquals("123 Main St", trainee.getAddress());
    }

    @Test
    void toTrainee_MapCorrectly_FromUpdateRequest() {
        UpdateTraineeRequest request = mock(UpdateTraineeRequest.class);
        when(request.id()).thenReturn(1L);
        when(request.firstName()).thenReturn("John");
        when(request.lastName()).thenReturn("Doe");
        when(request.dateOfBirth()).thenReturn(LocalDate.of(1995, 5, 15));
        when(request.address()).thenReturn("456 New St");
        when(request.isActive()).thenReturn(true);

        Trainee trainee = gymMapper.toTrainee(request);
        assertNotNull(trainee);
        assertEquals(1L, trainee.getId());
        assertEquals("John", trainee.getFirstName());
        assertEquals("456 New St", trainee.getAddress());
        assertTrue(trainee.isActive());
    }

    @Test
    void toTraineeResponse_MapAndNestProfileCorrectly_FromTrainee() {
        Trainee trainee = new Trainee();
        trainee.setId(10L);
        trainee.setFirstName("Jane");
        trainee.setLastName("Smith");
        trainee.setUsername("Jane.Smith");
        trainee.setPassword("test122334");
        trainee.setActive(true);
        trainee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        trainee.setAddress("789 Oak Ave");

        TraineeResponse response = gymMapper.toTraineeResponse(trainee);
        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals(LocalDate.of(1990, 1, 1), response.dateOfBirth());
        assertEquals("789 Oak Ave", response.address());
        assertNotNull(response.profile());
        assertEquals("Jane", response.profile().firstName());
        assertEquals("Jane.Smith", response.profile().username());
        assertTrue(response.profile().isActive());
    }

    @Test
    void toTraineeSummary_MapCorrectly_FromTrainee() {
        Trainee trainee = new Trainee();
        trainee.setId(5L);
        trainee.setFirstName("Mark");
        trainee.setLastName("Twain");
        trainee.setUsername("Mark.Twain");

        TraineeSummary summary = gymMapper.toTraineeSummary(trainee);
        assertNotNull(summary);
        assertEquals(5L, summary.id());
        assertEquals("Mark", summary.firstName());
        assertEquals("Mark.Twain", summary.username());
    }

    @Test
    void toTrainer_MapCorrectly_FromCreateRequest() {
        CreateTrainerRequest request = mock(CreateTrainerRequest.class);
        when(request.firstName()).thenReturn("Anna");
        when(request.lastName()).thenReturn("Example");
        when(request.specialization()).thenReturn(TrainingType.YOGA);

        Trainer trainer = gymMapper.toTrainer(request);
        assertNotNull(trainer);
        assertEquals("Anna", trainer.getFirstName());
        assertEquals("Example", trainer.getLastName());
        assertEquals(TrainingType.YOGA, trainer.getSpecialization());
    }

    @Test
    void toTrainee_ThrowException_NullCreateRequest() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> gymMapper.toTrainee((CreateTraineeRequest) null));
        assertEquals("Create request cannot be null", exception.getMessage());
    }

    @Test
    void toTraineeResponse_ThrowException_NullTrainee() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> gymMapper.toTraineeResponse(null));
        assertEquals("Trainee entity cannot be null", exception.getMessage());
    }

    @Test
    void toTrainer_MapCorrectly_FromUpdateRequest() {
        UpdateTrainerRequest request = mock(UpdateTrainerRequest.class);
        when(request.id()).thenReturn(2L);
        when(request.firstName()).thenReturn("Anna");
        when(request.lastName()).thenReturn("Example");
        when(request.specialization()).thenReturn(TrainingType.YOGA);
        when(request.isActive()).thenReturn(false);

        Trainer trainer = gymMapper.toTrainer(request);
        assertNotNull(trainer);
        assertEquals(2L, trainer.getId());
        assertEquals("Anna", trainer.getFirstName());
        assertEquals("Example", trainer.getLastName());
        assertEquals(TrainingType.YOGA, trainer.getSpecialization());
        assertFalse(trainer.isActive());
    }

    @Test
    void toTrainerResponse_MapAndNestProfileCorrectly_FromTrainer() {
        Trainer trainer = new Trainer();
        trainer.setId(3L);
        trainer.setFirstName("Arnold");
        trainer.setLastName("Schwarzenegger");
        trainer.setUsername("Arnold.Schwarzenegger");
        trainer.setPassword("terminator");
        trainer.setActive(true);
        trainer.setSpecialization(TrainingType.STRENGTH);

        TrainerResponse response = gymMapper.toTrainerResponse(trainer);
        assertNotNull(response);
        assertEquals(3L, response.id());
        assertEquals(TrainingType.STRENGTH, response.specialization());
        assertNotNull(response.profile());
        assertEquals("Arnold", response.profile().firstName());
        assertEquals("Arnold.Schwarzenegger", response.profile().username());
        assertTrue(response.profile().isActive());
    }

    @Test
    void toTrainerSummary_MapCorrectly_FromTrainer() {
        Trainer trainer = new Trainer();
        trainer.setId(4L);
        trainer.setFirstName("Bruce");
        trainer.setLastName("Lee");
        trainer.setUsername("Bruce.Lee");
        trainer.setSpecialization(TrainingType.STRENGTH);

        TrainerSummary summary = gymMapper.toTrainerSummary(trainer);
        assertNotNull(summary);
        assertEquals(4L, summary.id());
        assertEquals("Bruce", summary.firstName());
        assertEquals("Bruce.Lee", summary.username());
        assertEquals(TrainingType.STRENGTH, summary.specialization());
    }

    @Test
    void toTraining_MapFromRequestAndEntitiesCorrectly_FromCreateRequestAndTraineeAndTrainer() {
        CreateTrainingRequest request = mock(CreateTrainingRequest.class);
        when(request.trainingName()).thenReturn("Morning Cardio");
        when(request.trainingDate()).thenReturn(LocalDate.of(2023, 10, 1));
        when(request.duration()).thenReturn(60);
        Trainee trainee = new Trainee();
        trainee.setId(100L);
        Trainer trainer = new Trainer();
        trainer.setId(200L);
        trainer.setSpecialization(TrainingType.CARDIO);

        Training training = gymMapper.toTraining(request, trainee, trainer);
        assertNotNull(training);
        assertEquals(100L, training.getTraineeId());
        assertEquals(200L, training.getTrainerId());
        assertEquals("Morning Cardio", training.getTrainingName());
        assertEquals(TrainingType.CARDIO, training.getTrainingType());
        assertEquals(LocalDate.of(2023, 10, 1), training.getTrainingDate());
        assertEquals(60, training.getDuration());
    }

    @Test
    void toTrainingResponse_MapAndNestSummariesCorrectly_FromTrainingAndTraineeAndTrainer() {
        Training training = new Training();
        training.setId(50L);
        training.setTrainingName("Powerlifting Basics");
        training.setTrainingType(TrainingType.STRENGTH);
        training.setTrainingDate(LocalDate.of(2023, 11, 15));
        training.setDuration(90);
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        trainee.setUsername("John.Doe");
        Trainer trainer = new Trainer();
        trainer.setId(2L);
        trainer.setFirstName("Jane");
        trainer.setLastName("Smith");
        trainer.setUsername("Jane.Smith");
        trainer.setSpecialization(TrainingType.STRENGTH);

        TrainingResponse response = gymMapper.toTrainingResponse(training, trainee, trainer);

        assertNotNull(response);
        assertEquals(50L, response.id());
        assertEquals("Powerlifting Basics", response.trainingName());
        assertEquals(TrainingType.STRENGTH, response.trainingType());
        assertEquals(LocalDate.of(2023, 11, 15), response.trainingDate());
        assertEquals(90, response.duration());
        assertNotNull(response.trainer());
        assertEquals(2L, response.trainer().id());
        assertEquals("Jane", response.trainer().firstName());
        assertNotNull(response.trainee());
        assertEquals(1L, response.trainee().id());
        assertEquals("John", response.trainee().firstName());
    }

    @Test
    void toTraining_ThrowException_NullRequest() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> gymMapper.toTraining(null, new Trainee(), new Trainer()));
        assertEquals("Create request cannot be null", exception.getMessage());
    }

    @Test
    void toTrainingResponse_ThrowException_NullTraining() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> gymMapper.toTrainingResponse(null, new Trainee(), new Trainer()));
        assertEquals("Training entity cannot be null", exception.getMessage());
    }
}
