package org.example.crm.macrocycle.controller.request;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = TargetEventDateValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTargetEventDate {
    String message() default "Target event date must be at least 12 weeks from today";
    int minWeeks() default 12;

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
