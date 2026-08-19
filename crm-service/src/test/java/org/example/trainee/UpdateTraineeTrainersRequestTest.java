package org.example.trainee;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.example.trainee.controller.request.UpdateTraineeTrainersRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UpdateTraineeTrainersRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void trainerUsernames_PassValidation_AllUsernamesAreValid() {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(
                List.of("John.Doe", "Jane.Smith")
        );

        Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void trainerUsernames_FailValidation_UsernameInListIsBlank() {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(
                List.of("John.Doe", "   ", "Jane.Smith")
        );

        Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        ConstraintViolation<UpdateTraineeTrainersRequest> violation = violations.iterator().next();
        assertEquals("Username in the list cannot be blank", violation.getMessage());
    }

    @Test
    void trainerUsernames_FailValidation_ListIsEmpty() {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(List.of());

        Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        ConstraintViolation<UpdateTraineeTrainersRequest> violation = violations.iterator().next();
        assertEquals("Trainers usernames list cannot be empty", violation.getMessage());
    }
}
