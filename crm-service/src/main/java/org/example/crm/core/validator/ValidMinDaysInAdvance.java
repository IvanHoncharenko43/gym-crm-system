package org.example.crm.core.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Documented
@Constraint(validatedBy = MinDaysInAdvanceValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMinDaysInAdvance {
    String message() default "Training must be planned at least 1 day in advance";
    int value() default 1;

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
