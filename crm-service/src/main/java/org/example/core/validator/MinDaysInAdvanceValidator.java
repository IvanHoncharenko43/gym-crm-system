package org.example.core.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class MinDaysInAdvanceValidator implements ConstraintValidator<ValidMinDaysInAdvance, LocalDate> {
    private int minDays;

    @Override
    public void initialize(ValidMinDaysInAdvance constraintAnnotation) {
        this.minDays = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.isAfter(LocalDate.now().plusDays(minDays - 1));
    }
}
