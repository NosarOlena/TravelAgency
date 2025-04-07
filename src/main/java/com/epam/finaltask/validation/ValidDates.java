package com.epam.finaltask.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DateValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDates {
    String message() default "{validation.date.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
