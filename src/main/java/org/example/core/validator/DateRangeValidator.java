package org.example.core.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, DateRangeProvider> {
    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(DateRangeProvider value, ConstraintValidatorContext context) {
        if (value == null || value.fromDate() == null || value.toDate() == null) {
            return true;
        }
        boolean isValid = value.fromDate().isBefore(value.toDate());
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("fromDate")
                    .addConstraintViolation();
        }
        return isValid;
    }
}
