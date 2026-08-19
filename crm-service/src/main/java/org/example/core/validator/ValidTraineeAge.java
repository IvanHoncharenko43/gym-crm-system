package org.example.core.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TraineeAgeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTraineeAge {
    String message () default "Trainee must be at least 14 years old";
    int minimumAge() default 14;

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
