package com.backend.hotelReservationSystem.comp;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtLeastOneFieldValidator.class)
@Documented
public @interface AtLeastOneField {
    String message() default "At least one field must be provided";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
