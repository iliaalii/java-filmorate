package ru.yandex.practicum.filmorate.model.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AfterDateValidator.class)
public @interface AfterDate {
    String message() default "Дата должна быть позднее {value}";
    String value();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
