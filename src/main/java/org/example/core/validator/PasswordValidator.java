package org.example.core.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private int minLength;
    private int maxLength;
    private static final String COMPLEXITY_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$";

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.maxLength = constraintAnnotation.maxLength();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.trim().isEmpty()) {
            return true;
        }
        boolean isValid = true;
        context.disableDefaultConstraintViolation();
        if (password.length() < minLength) {
            context.buildConstraintViolationWithTemplate("Password must be at least " + minLength + " characters long")
                    .addConstraintViolation();
            isValid = false;
        }
        if (password.length() > maxLength) {
            context.buildConstraintViolationWithTemplate("Password must not be greater than " + maxLength + " characters long")
                    .addConstraintViolation();
            isValid = false;
        }
        if (!password.matches(COMPLEXITY_REGEX)) {
            context.buildConstraintViolationWithTemplate("Password must contain at least one digit, one uppercase letter, one lowercase letter, and one special character")
                    .addConstraintViolation();
            isValid = false;
        }
        return isValid;
    }
}
