package org.example.core.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class TraineeAgeValidator implements ConstraintValidator<ValidTraineeAge, LocalDate> {

    private int minimumAge;

    @Override
    public void initialize(ValidTraineeAge constraintAnnotation) {
        this.minimumAge = constraintAnnotation.minimumAge();
    }

    @Override
    public boolean isValid(LocalDate dateOfBirth, ConstraintValidatorContext context) {
        if(dateOfBirth == null){
            return true;
        }
        long exactAge = ChronoUnit.YEARS.between(dateOfBirth, LocalDate.now());
        return exactAge >= minimumAge;
    }
}
