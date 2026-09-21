package org.example.crm.macrocycle.controller.request;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class TargetEventDateValidator implements ConstraintValidator<ValidTargetEventDate, LocalDate> {
    private int minWeeks;

    @Override
    public void initialize(ValidTargetEventDate constraintAnnotation) {
        this.minWeeks = constraintAnnotation.minWeeks();
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return !value.isBefore(LocalDate.now().plusWeeks(minWeeks));
    }
}
